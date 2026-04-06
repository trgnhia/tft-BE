package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.core.api.PageResponse;
import org.example.dto.champs.ChampResponse;
import org.example.dto.champs.CreateChampRequest;
import org.example.dto.champs.UpdateChampRequest;
import org.example.entities.Sets;
import org.example.entities.champ.Champ;
import org.example.mapper.ChampsMapper;
import org.example.repositories.ChampRepository;
import org.example.repositories.SetsRepository;
import org.example.services.BaseService;
import org.example.services.ChampService;
import org.example.util.FilterUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                        Constants.MessageKey.ERROR_NOT_FOUND));
    }

    @Override
    public ChampResponse getBySlug(String slug) {
        log.info("[CHAMP] Fetching champ by slug: {}", slug);
        filterUtil.enableDeletedFilter();
        return champRepository.findBySlug(slug)
                .map(champMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.CHAMP_SLUG_NOT_FOUND));
    }

    @Override
    @Transactional
    public ChampResponse create(CreateChampRequest request) {
        log.info("[CHAMP] Creating new champ with slug: {}", request.getSlug());
        if (champRepository.existsBySlug(request.getSlug())) {
            log.warn("[CHAMP] Create failed: Slug {} already exists", request.getSlug());
            throw new ConflictException(Constants.MessageKey.ERROR_ALREADY_EXIST, request.getSlug());
        }

        Sets sets = setsRepository.findById(request.getSetId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND));

        Champ champ = champMapper.toEntity(request);
        champ.setSets(sets);

        Champ savedChamp = champRepository.save(champ);
        log.info("[CHAMP] Created id={} slug={} by user={}",
                savedChamp.getId(), savedChamp.getSlug(), getCurrentUserNameOrThrow());
        return champMapper.toResponse(champRepository.save(champ));
    }

    @Override
    @Transactional
    public ChampResponse update(Long id, UpdateChampRequest request) {
        log.info("[CHAMP] Updating champ id: {}", id);
        filterUtil.enableDeletedFilter();

        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.CHAMP_NOT_FOUND));

        if (request.getSlug() != null && !request.getSlug().equals(champ.getSlug())
                && champRepository.existsBySlugAndIdNot(request.getSlug(), id)) {
            log.warn("[CHAMP] Update failed: Slug {} already taken by another record", request.getSlug());
            throw new ConflictException(Constants.MessageKey.ERROR_ALREADY_EXIST, request.getSlug());
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
                        Constants.MessageKey.CHAMP_NOT_FOUND));

        champRepository.delete(champ);
        log.info("[CHAMP] Deleted successfully id={} by user={}", id, getCurrentUserNameOrThrow());
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
                        Constants.MessageKey.CHAMP_NOT_FOUND));
    }

    @Override
    public void restore(Long id) {
        log.info("[CHAMP-ADMIN] Restoring deleted champ id: {}", id);
        filterUtil.disableDeletedFilter();
        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Constants.MessageKey.ERROR_NOT_FOUND));

        if (!champ.isDeleted()) {
            log.warn("[CHAMP-ADMIN] Restore failed: Champ id={} is not in deleted state", id);
            throw new DataException(ErrorCode.INVALID_PARAMETER, Constants.MessageKey.ENTITY_CHAMP);
        }

        champ.setDeleted(false);
        log.info("[CHAMP-ADMIN] Restored successfully id={} by user={}", id, getCurrentUserNameOrThrow());
    }
}
