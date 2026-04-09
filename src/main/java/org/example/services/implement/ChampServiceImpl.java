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
import org.example.mapper.ChampsMapper;
import org.example.repositories.ChampRepository;
import org.example.repositories.spec.ChampSpecification;
import org.example.repositories.SetsRepository;
import org.example.services.BaseService;
import org.example.services.ChampService;
import org.example.util.FilterUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChampServiceImpl extends BaseService implements ChampService {
    private final ChampRepository champRepository;
    private final SetsRepository setsRepository;
    private final ChampsMapper champMapper;
    private final FilterUtil filterUtil;

    @Override
    public PageResponse<ChampResponse> getAll(String keyword, Pageable pageable) {
        log.info("[CHAMP] Fetching all champs with keyword: {}, page: {}", keyword, pageable.getPageNumber());
        filterUtil.enableDeletedFilter();
        Page<Champ> page = (keyword != null && !keyword.isBlank())
                ? champRepository.findByNameContainingIgnoreCase(keyword.trim(), pageable)
                : champRepository.findAll(pageable);
        return PageResponse.from(page.map(champMapper::toResponse));
    }

    @Override
    public ChampResponse getById(Long id) {
        log.info("[CHAMP] Fetching champ by id: {}", id);
        filterUtil.enableDeletedFilter();
        return champRepository.findById(id)
                .map(champMapper::toResponse)
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
                .map(champMapper::toResponse)
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
        return PageResponse.from(page.map(champMapper::toResponse));
    }

    @Override
    public List<ChampResponse> getBySetId(Long setId) {
        log.info("[CHAMP] getBySetId setId={}", setId);
        filterUtil.enableDeletedFilter();
        return champRepository.findBySetsId(setId)
                .stream()
                .map(champMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChampResponse create(CreateChampRequest request) {
        if (champRepository.existsBySlug(request.getSlug())) {
            throw new ConflictException(Constants.MessageKey.ERROR_ALREADY_EXISTS, request.getSlug());
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
        log.info("[CHAMP] Created id={} slug={} by user={}",
                savedChamp.getId(), savedChamp.getSlug(), getCurrentUserNameOrThrow());
        return champMapper.toResponse(savedChamp);
    }

    @Override
    @Transactional
    public ChampResponse update(Long id, UpdateChampRequest request) {
        log.info("[CHAMP] Updating champ id: {}", id);
        filterUtil.enableDeletedFilter();

        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));

        if (request.getSlug() != null && !request.getSlug().equals(champ.getSlug())
                && champRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            log.warn("[CHAMP] Update failed: Slug {} already taken by another record", request.getSlug());
            throw new ConflictException(Constants.MessageKey.ERROR_ALREADY_EXISTS, request.getSlug());
        }

        champMapper.updateEntity(request, champ);

        log.info("[CHAMP] Updated successfully id={} by user={}", id, getCurrentUserNameOrThrow());
        return champMapper.toResponse(champ);
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

        champRepository.delete(champ);
        log.info("[CHAMP] Deleted successfully id={} by user={}", id, getCurrentUserNameOrThrow());
    }

    @Override
    @Transactional
    public List<ChampResponse> bulkCreate(BulkCreateRequest request) {
        log.info("[CHAMP] bulkCreate count={}", request.getChamps().size());
        return request.getChamps().stream()
                .map(this::create)
                .collect(Collectors.toList());
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
        return PageResponse.from(page.map(champMapper::toResponse));
    }

    @Override
    public ChampResponse getByIdAdmin(Long id) {
        log.info("[CHAMP-ADMIN] Fetching champ id: {} including deleted", id);
        filterUtil.disableDeletedFilter();
        return champRepository.findById(id)
                .map(champMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));
    }

    @Override
    public void restore(Long id) {
        log.info("[CHAMP-ADMIN] Restoring deleted champ id: {}", id);
        filterUtil.disableDeletedFilter();
        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}
                ));

        if (!champ.isDeleted()) {
            log.warn("[CHAMP-ADMIN] Restore failed: Champ id={} is not in deleted state", id);
            throw new DataException(ErrorCode.INVALID_PARAMETER,
                    new Object[]{Constants.MessageKey.ENTITY_CHAMP});
        }

        champ.setDeleted(false);
        log.info("[CHAMP-ADMIN] Restored successfully id={} by user={}", id, getCurrentUserNameOrThrow());
    }

    @Override
    public PageResponse<ChampResponse> searchAdmin(ChampFilterRequest filter, Pageable pageable) {
        log.info("[CHAMP-ADMIN] searchAdmin filter={}", filter);
        filterUtil.disableDeletedFilter();
        Specification<Champ> spec = ChampSpecification.withFilter(filter);
        Page<Champ> page = champRepository.findAll(spec, pageable);
        return PageResponse.from(page.map(champMapper::toResponse));
    }


    @Override
    @Transactional
    public void bulkRestore(BulkDeleteRequest request) {
        log.info("[CHAMP-ADMIN] bulkRestore ids={}", request.getIds());
        filterUtil.disableDeletedFilter();
        List<Champ> champs = champRepository.findAllById(request.getIds());
        champs.forEach(c -> {
            if (!c.isDeleted()) {
                throw new DataException(ErrorCode.INVALID_PARAMETER,
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP});
            }
            c.setDeleted(false);
        });
        log.info("[CHAMP-ADMIN] bulkRestored count={} by user={}", champs.size(), getCurrentUserNameOrThrow());
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

        Map<String, Long> byTier = champRepository.countGroupByTier()
                .stream().collect(Collectors.toMap(
                        r -> (String) r[0], r -> (Long) r[1]));

        return ChampOverviewStatsResponse.builder()
                .totalChamps(total)
                .totalDeleted(deleted)
                .totalActive(active)
                .countBySet(bySet)
                .countByCost(byCost)
                .countByTier(byTier)
                .build();
    }

}
