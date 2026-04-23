package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.core.api.PageResponse;
import org.example.dto.champs.*;
import org.example.entities.Sets;
import org.example.entities.champ.Champ;
import org.example.entities.champ.ChampTrait;
import org.example.entities.trait.Trait;
import org.example.mapper.ChampsMapper;
import org.example.repositories.ChampItemRecommendRepository;
import org.example.repositories.ChampRepository;
import org.example.repositories.ChampTraitRepository;
import org.example.repositories.projection.ChampRestoreCandidate;
import org.example.repositories.spec.ChampSpecification;
import org.example.repositories.SetsRepository;
import org.example.repositories.TraitRepository;
import org.example.services.AssetUrlService;
import org.example.services.BaseService;
import org.example.services.ChampService;
import org.example.services.FileStorageService;
import org.example.util.FilterUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChampServiceImpl extends BaseService implements ChampService {
    private final ChampRepository champRepository;
    private final SetsRepository setsRepository;
    private final ChampsMapper champMapper;
    private final ChampItemRecommendRepository champItemRecommendRepo;
    private final ChampTraitRepository champTraitRepository;
    private final TraitRepository traitRepository;
    private final FilterUtil filterUtil;
    private final FileStorageService fileStorageService;
    private final AssetUrlService assetUrlService;

    @Override
    public PageResponse<ChampResponse> getAll(String keyword, Pageable pageable) {
        log.info("[CHAMP] Fetching all champs with keyword: {}, page: {}", keyword, pageable.getPageNumber());
        filterUtil.enableDeletedFilter();
        Page<Champ> page = (keyword != null && !keyword.isBlank())
                ? champRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                : champRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public ChampResponse getById(Long id) {
        log.info("[CHAMP] Fetching champ by id: {}", id);
        filterUtil.enableDeletedFilter();
        return champRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));
    }

    @Override
    public ChampResponse getBySlug(String slug) {
        log.info("[CHAMP] Fetching champ by slug: {}", slug);
        filterUtil.enableDeletedFilter();
        return champRepository.findBySlug(slug)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));
    }

    @Override
    public PageResponse<ChampResponse> search(ChampFilterRequest filter, Pageable pageable) {
        log.info("[CHAMP] search filter={} page={}", filter, pageable.getPageNumber());
        filterUtil.enableDeletedFilter();
        // không cho phép public filter deleted
        filter.setDeleted(null);
        Specification<Champ> spec = ChampSpecification.withFilter(filter);
        Page<Champ> page = champRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public List<ChampResponse> getBySetId(Long setId) {
        log.info("[CHAMP] getBySetId setId={}", setId);
        filterUtil.enableDeletedFilter();
        return champRepository.findBySetsId(setId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public List<ChampResponse> getAllSortedByNameAsc(Long setId) {
        log.info("[CHAMP] getAllSortedByNameAsc setId={}", setId);
        filterUtil.enableDeletedFilter();
        List<Champ> champs = setId == null
                ? champRepository.findAllByDeletedFalseOrderByNameAsc()
                : champRepository.findAllBySetsIdAndDeletedFalseOrderByNameAsc(setId);
        return champs.stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChampResponse create(CreateChampRequest request) {
        filterUtil.enableDeletedFilter();
        String resolvedCode = resolveTargetCode(request.getCode(), request.getSlug(), null, null);
        request.setCode(resolvedCode);

        if (champRepository.existsBySlugIncludingDeleted(request.getSlug())) {
            throw new ConflictException(Constants.MessageKey.CHAMP_SLUG_EXISTS, request.getSlug());
        }
        if (champRepository.existsByCodeIncludingDeleted(resolvedCode)) {
            throw new ConflictException(Constants.MessageKey.CHAMP_CODE_EXISTS, resolvedCode);
        }
        Sets sets = setsRepository.findById(request.getSetId())
                .orElseThrow(() -> {
                    log.warn("[CHAMP] SetId {} not found", request.getSetId());
                    return new ResourceNotFoundException(
                            Constants.MessageKey.ERROR_NOT_FOUND,
                            new Object[]{Constants.MessageKey.ENTITY_SETS, request.getSetId().toString()}
                    );
                });

        Champ champ = champMapper.toEntity(request);
        champ.setSets(sets);

        Champ savedChamp = champRepository.save(champ);
        syncChampTraits(savedChamp, request.getSetId(), request.getTraitIds());
        log.info("[CHAMP] Created id={} slug={} by user={}",
                savedChamp.getId(), savedChamp.getSlug(), getCurrentUserNameOrThrow());
        return toResponse(savedChamp);
    }

    @Override
    @Transactional
    public ChampResponse update(Long id, UpdateChampRequest request) {
        return updateInternal(id, request, false);
    }

    @Override
    @Transactional
    public ChampResponse updateForImport(Long id, UpdateChampRequest request) {
        return updateInternal(id, request, true);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("[CHAMP] Deleting champ id: {}", id);
        filterUtil.enableDeletedFilter();
        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));
        champItemRecommendRepo.softDeleteByChampionId(id);
        champRepository.delete(champ);
        log.info("[CHAMP] Deleted successfully id={} by user={}", id, getCurrentUserNameOrThrow());
    }

    @Override
    @Transactional
    public List<ChampResponse> bulkCreate(BulkCreateRequest request) {
        log.info("[CHAMP] bulkCreate count={}", request.getChamps().size());
        return request.getChamps().stream()
                .map(this::create)
                .toList();
    }

    @Override
    @Transactional
    public void bulkDelete(BulkDeleteRequest request) {
        log.info("[CHAMP] bulkDelete ids={}", request.getIds());
        filterUtil.enableDeletedFilter();
        List<Champ> champs = champRepository.findAllById(request.getIds());
        if (champs.size() != request.getIds().size()) {
            throw new ResourceNotFoundException(
                    Constants.MessageKey.ERROR_NOT_FOUND,
                    new Object[]{Constants.MessageKey.ENTITY_CHAMP}
            );
        }
        champRepository.deleteAll(champs);
        log.info("[CHAMP] bulkDeleted count={} by user={}", champs.size(), getCurrentUserNameOrThrow());
    }

    @Override
    public PageResponse<ChampResponse> getAllAdmin(String keyword, Pageable pageable) {
        log.info("[CHAMP-ADMIN] Fetching all champs including deleted, keyword: {}", keyword);
        filterUtil.disableDeletedFilter();
        Page<Champ> page = (keyword != null && !keyword.isBlank())
                ? champRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                : champRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Override
    public ChampResponse getByIdAdmin(Long id) {
        log.info("[CHAMP-ADMIN] Fetching champ id: {} including deleted", id);
        filterUtil.disableDeletedFilter();
        return champRepository.findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));
    }

    @Override
    @Transactional
    public void restore(Long id) {
        log.info("[CHAMP-ADMIN] Restoring deleted champ id: {}", id);
        filterUtil.disableDeletedFilter();
        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));

        if (!champ.isDeleted()) {
            log.info("[CHAMP-ADMIN] Restore skipped: Champ id={} is already active", id);
            return;
        }

        validateParentSetCanRestore(champ.getSets());

        champ.setDeleted(false);
        log.info("[CHAMP-ADMIN] Restored successfully id={} by user={}", id, getCurrentUserNameOrThrow());
    }

    @Override
    public PageResponse<ChampResponse> searchAdmin(ChampFilterRequest filter, Pageable pageable) {
        log.info("[CHAMP-ADMIN] searchAdmin filter={}", filter);
        filterUtil.disableDeletedFilter();
        Specification<Champ> spec = ChampSpecification.withFilter(filter);
        Page<Champ> page = champRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }


    @Override
    @Transactional
    public BulkRestoreChampResponse bulkRestore(BulkDeleteRequest request) {
        log.info("[CHAMP-ADMIN] bulkRestore ids={}", request.getIds());
        filterUtil.disableDeletedFilter();
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return BulkRestoreChampResponse.builder().build();
        }

        List<Long> requestedIds = request.getIds().stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (requestedIds.isEmpty()) {
            return BulkRestoreChampResponse.builder().build();
        }

        List<ChampRestoreCandidate> candidates = champRepository.findRestoreCandidatesByIds(requestedIds);
        Set<Long> foundIds = candidates.stream()
                .map(ChampRestoreCandidate::getChampId)
                .collect(Collectors.toSet());

        List<Long> restorableIds = new ArrayList<>();
        List<Long> restoredIds = new ArrayList<>();
        List<BulkRestoreChampFailedItem> failedItems = new ArrayList<>();

        for (ChampRestoreCandidate candidate : candidates) {
            if (candidate.getSetId() == null) {
                failedItems.add(failedItem(candidate.getChampId(), "parent_set_not_found"));
                continue;
            }

            if (Boolean.TRUE.equals(candidate.getSetDeleted())) {
                failedItems.add(failedItem(candidate.getChampId(), "parent_set_deleted"));
                continue;
            }

            restorableIds.add(candidate.getChampId());
            restoredIds.add(candidate.getChampId());
        }

        requestedIds.stream()
                .filter(id -> !foundIds.contains(id))
                .forEach(id -> failedItems.add(failedItem(id, "champ_not_found")));

        if (!restorableIds.isEmpty()) {
            champRepository.bulkRestoreByIds(restorableIds);
        }

        log.info("[CHAMP-ADMIN] bulkRestore completed restored={} failed={} by user={}",
                restoredIds.size(), failedItems.size(), getCurrentUserNameOrThrow());

        return BulkRestoreChampResponse.builder()
                .restored(restoredIds)
                .failed(failedItems)
                .build();
    }

    @Override
    public ChampOverviewStatsResponse getStats() {
        log.info("[CHAMP-ADMIN] getStats");
        filterUtil.disableDeletedFilter();

        long total   = champRepository.count();
        long deleted = champRepository.countByDeletedTrue();
        long active  = champRepository.countByDeletedFalse();

        Map<String, Long> bySet = champRepository.countGroupBySet()
                .stream().collect(Collectors.toMap(
                        r -> (String) r[0], r -> (Long) r[1]));

        Map<Integer, Long> byCost = champRepository.countGroupByCost()
                .stream().collect(Collectors.toMap(
                        r -> (Integer) r[0], r -> (Long) r[1]));

        return ChampOverviewStatsResponse.builder()
                .totalChamps(total)
                .totalDeleted(deleted)
                .totalActive(active)
                .countBySet(bySet)
                .countByCost(byCost)
                .build();
    }

    private ChampResponse updateInternal(Long id, UpdateChampRequest request, boolean includeDeleted) {
        log.info("[CHAMP] Updating champ id: {} includeDeleted={}", id, includeDeleted);
        if (includeDeleted) {
            filterUtil.disableDeletedFilter();
        } else {
            filterUtil.enableDeletedFilter();
        }

        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));

        if (includeDeleted && champ.isDeleted()) {
            champ.setDeleted(false);
        }

        String targetSlug = normalizeText(request.getSlug());
        if (targetSlug != null) {
            request.setSlug(targetSlug);
        }
        if (targetSlug != null && !targetSlug.equalsIgnoreCase(champ.getSlug())
                && champRepository.existsBySlugAndIdNotIncludingDeleted(targetSlug, id)) {
            log.warn("[CHAMP] Update failed: Slug {} already taken by another record", targetSlug);
            throw new ConflictException(Constants.MessageKey.ERROR_ALREADY_EXISTS, targetSlug);
        }

        String targetCode = resolveTargetCode(
                request.getCode(),
                targetSlug != null ? targetSlug : request.getSlug(),
                champ.getCode(),
                champ.getSlug()
        );
        if (targetCode != null && !targetCode.equalsIgnoreCase(champ.getCode())
                && champRepository.existsByCodeAndIdNotIncludingDeleted(targetCode, id)) {
            log.warn("[CHAMP] Update failed: Code {} already taken by another record", targetCode);
            throw new ConflictException(Constants.MessageKey.CHAMP_CODE_EXISTS, targetCode);
        }
        request.setCode(targetCode);

        String oldImageUrl = champ.getImageUrl();

        Long targetSetId = resolveTargetSetId(request, champ);
        Long currentSetId = champ.getSets() != null ? champ.getSets().getId() : null;
        if (!Objects.equals(targetSetId, currentSetId)) {
            if (targetSetId == null) {
                champ.setSets(null);
            } else {
                Sets targetSet = getOrThrowSet(targetSetId);
                champ.setSets(targetSet);
            }
        }

        champMapper.updateEntity(request, champ);
        syncChampTraits(champ, targetSetId, request.getTraitIds());
        deleteObsoleteManagedImage(oldImageUrl, champ.getImageUrl());

        log.info("[CHAMP] Updated successfully id={} by user={}", id, getCurrentUserNameOrThrow());
        return toResponse(champ);
    }

    private Long resolveTargetSetId(UpdateChampRequest request, Champ champ) {
        if (request.getSetId() != null) {
            return request.getSetId();
        }
        return champ.getSets() != null ? champ.getSets().getId() : null;
    }

    private String resolveTargetCode(String requestedCode, String requestedSlug, String currentCode, String currentSlug) {
        String normalizedRequestedCode = normalizeText(requestedCode);
        if (normalizedRequestedCode != null) {
            return normalizedRequestedCode;
        }

        String normalizedCurrentCode = normalizeText(currentCode);
        if (normalizedCurrentCode != null) {
            return normalizedCurrentCode;
        }

        String normalizedRequestedSlug = normalizeText(requestedSlug);
        if (normalizedRequestedSlug != null) {
            return normalizedRequestedSlug;
        }

        return normalizeText(currentSlug);
    }

    private String normalizeText(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Sets getOrThrowSet(Long setId) {
        return setsRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_SETS, String.valueOf(setId)}
                ));
    }

    private void syncChampTraits(Champ champ, Long targetSetId, List<Long> traitIds) {
        if (traitIds == null) {
            return;
        }

        List<Long> normalizedIds = traitIds.stream()
                .filter(Objects::nonNull)
                .toList();
        ensureNoDuplicateTraitIds(normalizedIds);

        Set<Long> requestedTraitIds = new HashSet<>(normalizedIds);

        List<ChampTrait> existingLinks = champTraitRepository.findByChamp_Id(champ.getId());
        Set<Long> existingTraitIds = existingLinks.stream()
                .map(link -> link.getTrait().getId())
                .collect(Collectors.toSet());

        if (requestedTraitIds.isEmpty()) {
            if (!existingLinks.isEmpty()) {
                champTraitRepository.deleteAllInBatch(existingLinks);
            }
            champ.setChampTraits(new ArrayList<>());
            return;
        }

        List<Trait> traits = traitRepository.findAllById(normalizedIds);
        ensureAllTraitsExist(normalizedIds, traits);
        ensureTraitsBelongToSet(targetSetId, traits);

        List<ChampTrait> linksToDelete = existingLinks.stream()
                .filter(link -> !requestedTraitIds.contains(link.getTrait().getId()))
                .toList();
        if (!linksToDelete.isEmpty()) {
            champTraitRepository.deleteAllInBatch(linksToDelete);
        }

        List<ChampTrait> linksToAdd = traits.stream()
                .filter(trait -> !existingTraitIds.contains(trait.getId()))
                .map(trait -> ChampTrait.builder()
                        .champ(champ)
                        .trait(trait)
                        .build())
                .toList();
        if (!linksToAdd.isEmpty()) {
            champTraitRepository.saveAll(linksToAdd);
        }

        List<ChampTrait> updatedLinks = champTraitRepository.findByChamp_Id(champ.getId());
        champ.setChampTraits(new ArrayList<>(updatedLinks));
    }

    private void ensureNoDuplicateTraitIds(List<Long> traitIds) {
        Set<Long> uniqueTraitIds = new HashSet<>(traitIds);
        if (uniqueTraitIds.size() != traitIds.size()) {
            throw new DataException(ErrorCode.INVALID_PARAMETER, new Object[]{"Duplicate traitId in request"});
        }
    }

    private void ensureAllTraitsExist(List<Long> requestedTraitIds, List<Trait> traits) {
        Set<Long> foundIds = traits.stream()
                .map(Trait::getId)
                .collect(Collectors.toSet());
        List<Long> missingIds = requestedTraitIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new ResourceNotFoundException(
                    Constants.MessageKey.ERROR_NOT_FOUND,
                    new Object[]{Constants.MessageKey.ENTITY_TRAIT, missingIds.toString()}
            );
        }
    }

    private void ensureTraitsBelongToSet(Long setId, List<Trait> traits) {
        List<Long> mismatchedTraitIds = traits.stream()
                .filter(trait -> !Objects.equals(trait.getSets().getId(), setId))
                .map(Trait::getId)
                .toList();
        if (!mismatchedTraitIds.isEmpty()) {
            throw new DataException(
                    ErrorCode.INVALID_PARAMETER,
                    new Object[]{"traitIds " + mismatchedTraitIds + " do not belong to setId " + setId}
            );
        }
    }

    private void deleteObsoleteManagedImage(String oldImageUrl, String newImageUrl) {
        if (Objects.equals(oldImageUrl, newImageUrl) || !StringUtils.hasText(oldImageUrl)) {
            return;
        }
        fileStorageService.deleteManagedImageIfExists(oldImageUrl);
    }

    private ChampResponse toResponse(Champ champ) {
        ChampResponse response = champMapper.toResponse(champ);
        response.setImageUrl(assetUrlService.toPublicUrl(response.getImageUrl()));
        boolean setDeleted = champ.getSets() != null && champ.getSets().isDeleted();
        response.setSetDeleted(setDeleted);
        if (Boolean.TRUE.equals(response.getDeleted())) {
            boolean canRestore = !setDeleted;
            response.setCanRestore(canRestore);
            response.setRestoreBlockedReason(canRestore ? null : "SET_INACTIVE");
        } else {
            response.setCanRestore(false);
            response.setRestoreBlockedReason(null);
        }
        return response;
    }

    private void validateParentSetCanRestore(Sets parentSet) {
        if (parentSet == null) {
            throw new DataException(ErrorCode.INVALID_PARAMETER, new Object[]{"parent_set_not_found"});
        }
        if (parentSet.isDeleted()) {
            throw new DataException(ErrorCode.INVALID_PARAMETER, new Object[]{"parent_set_deleted"});
        }
    }

    private BulkRestoreChampFailedItem failedItem(Long champId, String reason) {
        return BulkRestoreChampFailedItem.builder()
                .champId(champId)
                .reason(reason)
                .build();
    }

}
