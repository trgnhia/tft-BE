package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.core.api.PageResponse;
import org.example.dto.champs.BulkDeleteRequest;
import org.example.dto.trait.*;
import org.example.entities.trait.Trait;
import org.example.mapper.TraitMapper;
import org.example.repositories.SetsRepository;
import org.example.repositories.TraitRepository;
import org.example.repositories.spec.TraitSpecification;
import org.example.services.AssetUrlService;
import org.example.services.BaseService;
import org.example.services.FileStorageService;
import org.example.services.TraitService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class TraitServiceImpl extends BaseService implements TraitService {

    private final TraitRepository traitRepository;
    private final SetsRepository setsRepository;
    private final TraitMapper traitMapper;
    private final FileStorageService fileStorageService;
    private final AssetUrlService assetUrlService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TraitResponse> getAll(String keyword, Pageable pageable) {
        log.info("[TRAIT] getAll keyword={}", keyword);
        return PageResponse.from(
                traitRepository.findAllActive(keyword, pageable)
                        .map(this::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TraitResponse getById(Long id) {
        log.info("[TRAIT] getById id={}", id);
        return toResponse(findActiveById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public TraitResponse getBySlug(String slug) {
        log.info("[TRAIT] getBySlug slug={}", slug);
        return toResponse(
                traitRepository.findBySlugAndDeletedFalse(slug)
                        .orElseThrow(() -> new ResourceNotFoundException(Constants.MessageKey.ENTITY_TRAIT))
        );
    }

    @Override
    @Transactional
    public TraitResponse create(CreateTraitRequest request) {
        log.info("[TRAIT] Creating slug={}", request.getSlug());

        if (traitRepository.existsBySlug(request.getSlug())) {
            log.warn("[TRAIT] Slug {} already exists", request.getSlug());
            throw new ConflictException(Constants.MessageKey.TRAIT_SLUG_EXISTS, request.getSlug());
        }

        var setEntity = setsRepository.findById(request.getSetId())
                .orElseThrow(() -> {
                    log.warn("[TRAIT] SetId {} not found", request.getSetId());
                    return new ResourceNotFoundException(Constants.MessageKey.ENTITY_SETS);
                });

        Trait trait = traitMapper.toEntity(request);

        trait.setSets(setEntity);

        Trait saved = traitRepository.save(trait);
        log.info("[TRAIT] Created id={} slug={}", saved.getId(), saved.getSlug());
        return toResponse(saved);
    }

    @Override
    @Transactional
    public TraitResponse update(Long id, UpdateTraitRequest request) {
        log.info("[TRAIT] Updating id={}", id);
        Trait trait = findActiveById(id);
        String oldIconUrl = trait.getIconUrl();
        var setEntity = setsRepository.findById(request.getSetId())
                .orElseThrow(() -> {
                    log.warn("[TRAIT] SetId {} not found during update", request.getSetId());
                    return new ResourceNotFoundException(Constants.MessageKey.ENTITY_SETS);
                });
        traitMapper.updateEntity(request, trait);
        trait.setSets(setEntity);

        Trait saved = traitRepository.save(trait);
        deleteObsoleteManagedImage(oldIconUrl, saved.getIconUrl());
        log.info("[TRAIT] Updated id={}", saved.getId());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public List<TraitResponse> bulkCreate(BulkCreateTraitRequest request) {
        log.info("[TRAIT] Bulk create count={}", request.getTraits() == null ? 0 : request.getTraits().size());
        if (request.getTraits() == null || request.getTraits().isEmpty()) {
            return List.of();
        }
        return request.getTraits().stream()
                .filter(Objects::nonNull)
                .map(this::create)
                .toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("[TRAIT] Deleting id={}", id);
        Trait trait = findActiveById(id);
        traitRepository.delete(trait);
        log.info("[TRAIT] Deleted id={}", id);
    }

    @Override
    @Transactional
    public void bulkDelete(BulkDeleteRequest request) {
        log.info("[TRAIT] Bulk delete ids={}", request.getIds());
        List<Trait> traits = traitRepository.findAllById(request.getIds());
        if (traits.size() != request.getIds().size()) {
            throw new ResourceNotFoundException(Constants.MessageKey.ENTITY_TRAIT);
        }
        traitRepository.deleteAll(traits);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TraitResponse> search(TraitFilterRequest filter, Pageable pageable) {
        log.info("[TRAIT] search filter={}", filter);
        Specification<Trait> spec = TraitSpecification.of(filter);
        return PageResponse.from(
                traitRepository.findAll(spec, pageable)
                        .map(this::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraitResponse> getForDropdown(Long setId) {
        log.info("[TRAIT] getForDropdown setId={}", setId);
        return traitRepository.findAllActiveForDropdown(setId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraitResponse> getBySetId(Long setId) {
        log.info("[TRAIT] getBySetId setId={}", setId);
        return traitRepository.findAllActiveForDropdown(setId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TraitResponse> getAllSortedByNameAsc(Long setId) {
        log.info("[TRAIT] getAllSortedByNameAsc setId={}", setId);
        return traitRepository.findAllActiveForDropdown(setId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TraitResponse> getAllAdmin(String keyword, Pageable pageable) {
        log.info("[TRAIT][ADMIN] getAll keyword={}", keyword);
        return PageResponse.from(
                traitRepository.findAllAdmin(keyword, pageable)
                        .map(this::toResponse)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public TraitResponse getByIdAdmin(Long id) {
        log.info("[TRAIT][ADMIN] getById id={}", id);
        return toResponse(
                traitRepository.findByIdAdmin(id)
                        .orElseThrow(() -> new ResourceNotFoundException(Constants.MessageKey.ENTITY_TRAIT))
        );
    }

    @Override
    @Transactional
    public void restore(Long id) {
        log.info("[TRAIT][ADMIN] Restoring id={}", id);
        Trait trait = traitRepository.findByIdAdmin(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MessageKey.ENTITY_TRAIT));
        trait.setDeleted(false);
        traitRepository.save(trait);
        log.info("[TRAIT][ADMIN] Restored id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TraitResponse> searchAdmin(TraitFilterRequest filter, Pageable pageable) {
        log.info("[TRAIT][ADMIN] searchAdmin filter={}", filter);

        if (Boolean.TRUE.equals(filter.getIncludeDeleted())) {
            Page<Trait> page = traitRepository.searchAdminIncludeDeleted(
                    filter.getKeyword(),
                    filter.getType(),
                    filter.getSetId(),
                    pageable
            );
            return PageResponse.from(page.map(this::toResponse));
        }

        Specification<Trait> spec = TraitSpecification.of(filter);
        return PageResponse.from(traitRepository.findAll(spec, pageable).map(this::toResponse));
    }


    @Override
    @Transactional
    public void bulkRestore(BulkDeleteRequest request) {
        log.info("[TRAIT] Bulk restore ids={}", request.getIds());
        if (request.getIds() == null || request.getIds().isEmpty()) {
            return;
        }
        traitRepository.restoreAllByIds(request.getIds());
    }

    @Override
    @Transactional(readOnly = true)
    public TraitOverviewStatsResponse getStats() {
        log.info("[TRAIT] Get overview stats");

        TraitOverviewStatsResponse stats = new TraitOverviewStatsResponse();

        stats.setTotal(traitRepository.countTotal());
        stats.setActive(traitRepository.countActive());
        stats.setDeleted(traitRepository.countDeleted());

        stats.setInactive(0L);

        return stats;
    }


    // helper
    private Trait findActiveById(Long id) {
        return traitRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(Constants.MessageKey.ENTITY_TRAIT));
    }

    private void deleteObsoleteManagedImage(String oldIconUrl, String newIconUrl) {
        if (Objects.equals(oldIconUrl, newIconUrl) || !org.springframework.util.StringUtils.hasText(oldIconUrl)) {
            return;
        }
        fileStorageService.deleteManagedImageIfExists(oldIconUrl);
    }

    private TraitResponse toResponse(Trait trait) {
        TraitResponse response = traitMapper.toResponse(trait);
        response.setIconUrl(assetUrlService.toPublicUrl(response.getIconUrl()));
        return response;
    }

}
