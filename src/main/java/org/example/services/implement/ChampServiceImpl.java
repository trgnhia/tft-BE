package org.example.services.implement;

import com.fasterxml.jackson.databind.ser.Serializers;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.core.api.PageResponse;
import org.example.dto.champs.ChampResponse;
import org.example.dto.champs.CreateChampRequest;
import org.example.dto.champs.UpdateChampRequest;
import org.example.entities.Champ;
import org.example.entities.Sets;
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

    private static final String ENTITY_NAME = "Champ";
    private final ChampRepository champRepository;
    private final SetsRepository setsRepository;
    private final ChampsMapper champMapper;
    private final FilterUtil filterUtil;

    @Override
    public PageResponse<ChampResponse> getAll(String keyword, Pageable pageable) {
        filterUtil.enableDeletedFilter();
        Page<Champ> page = keyword != null
                ? champRepository.findByNameContainingIgnoreCase(keyword, pageable)
                : champRepository.findAll(pageable);
        return PageResponse.from(page.map(champMapper::toResponse));
    }

    @Override
    public ChampResponse getById(Long id) {
        filterUtil.enableDeletedFilter();
        return champRepository.findById(id)
                .map(champMapper::toResponse)
                .orElseThrow(() -> new DataException(
                        ErrorCode.CHAMP_NOT_FOUND, ENTITY_NAME));
    }

    @Override
    public ChampResponse getBySlug(String slug) {
        filterUtil.enableDeletedFilter();
        return champRepository.findBySlug(slug)
                .map(champMapper::toResponse)
                .orElseThrow(() -> new DataException(
                        ErrorCode.CHAMP_NOT_FOUND, ENTITY_NAME));
    }

    @Override
    @Transactional
    public ChampResponse create(CreateChampRequest request) {
        filterUtil.enableDeletedFilter();

        if (champRepository.existsBySlug(request.getSlug()))
            throw new ConflictException("Champ slug");

        Sets sets = setsRepository.findById(request.getSetId())
                .orElseThrow(() -> new DataException(
                        ErrorCode.SET_NOT_FOUND, "Set"));

        Champ champ = champMapper.toEntity(request);
        champ.setSets(sets);

        ChampResponse response = champMapper.toResponse(champRepository.save(champ));
        log.info("[CHAMP] Created id={} slug={} by user={}",
                response.getId(), response.getSlug(), getCurrentUserName());
        return response;
    }

    @Override
    @Transactional
    public ChampResponse update(Long id, UpdateChampRequest request) {
        filterUtil.enableDeletedFilter();

        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new DataException(
                        ErrorCode.CHAMP_NOT_FOUND, ENTITY_NAME));

        if (request.getSlug() != null
                && champRepository.existsBySlugAndIdNot(request.getSlug(), id))
            throw new ConflictException("Champ slug");

        champMapper.updateEntity(request, champ);

        log.info("[CHAMP] Updated id={} by user={}", id, getCurrentUserName());
        return champMapper.toResponse(champ);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        filterUtil.enableDeletedFilter();

        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new DataException(
                        ErrorCode.CHAMP_NOT_FOUND, ENTITY_NAME));

        champRepository.delete(champ);
        log.info("[CHAMP] Deleted id={} by user={}", id, getCurrentUserName());

    }

    @Override
    public PageResponse<ChampResponse> getAllAdmin(String keyword, Pageable pageable) {
        filterUtil.disableDeletedFilter();
        Page<Champ> page = keyword != null
                ? champRepository.findByNameContainingIgnoreCase(keyword, pageable)
                : champRepository.findAll(pageable);
        return PageResponse.from(page.map(champMapper::toResponse));
    }

    @Override
    public ChampResponse getByIdAdmin(Long id) {
        filterUtil.disableDeletedFilter();
        return champRepository.findById(id)
                .map(champMapper::toResponse)
                .orElseThrow(() -> new DataException(
                        ErrorCode.CHAMP_NOT_FOUND, ENTITY_NAME));
    }

    @Override
    public void restore(Long id) {
        filterUtil.disableDeletedFilter();

        Champ champ = champRepository.findById(id)
                .orElseThrow(() -> new DataException(
                        ErrorCode.CHAMP_NOT_FOUND,ENTITY_NAME));

        if (!champ.isDeleted())
            throw new DataException(ErrorCode.CHAMP_NOT_DELETED, ENTITY_NAME);

        champ.setDeleted(false);
        log.info("[CHAMP] Restored id={} by user={}", id, getCurrentUserName());
    }
}
