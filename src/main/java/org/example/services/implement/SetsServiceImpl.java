package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.CacheNames;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.enums.NotificationTargetType;
import org.example.common.enums.NotificationType;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.core.api.PageResponse;
import org.example.dto.notification.NotificationCreateCommand;
import org.example.dto.sets.SetOptionResponse;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.entities.Sets;
import org.example.mapper.SetsMapper;
import org.example.repositories.SetsRepository;
import org.example.services.NotificationService;
import org.example.services.SetsService;
import org.example.util.MessageUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetsServiceImpl implements SetsService {

    private final SetsRepository setRepo;
    private final SetsMapper setsMapper;
    private final NotificationService notificationService;

    // ---------- PUBLIC SERVICES ----------
    @Override
    @Cacheable(cacheNames = CacheNames.PUBLIC_SETS, key = "'published:all'")
    public List<SetsResponse> getAllPublishedSet() {
        List<Sets> sets = setRepo.findAllByDeletedFalse();
        return setsMapper.toListSetsResponse(sets);
    }

    @Override
    @Cacheable(cacheNames = CacheNames.PUBLIC_SETS, key = "'options:public'")
    public List<SetOptionResponse> getSetOptions() {
        return setRepo.findAllByDeletedFalseOrderByNameAsc().stream()
                .map(this::toSetOption)
                .toList();
    }

    @Override
    public List<SetOptionResponse> getSetOptionsForCms() {
        return setRepo.findAllByOrderByNameAsc().stream()
                .map(this::toSetOption)
                .toList();
    }

    // ---------- CMS SERVICES ----------

    @Override
    public SetsResponse getSetById(Long id) {
        Sets sets = getById(id);
        return setsMapper.toSetsResponse(sets);
    }

    @Override
    public PageResponse<SetsResponse> getAllSet(int page, int size, String keyword, Boolean deleted) {
        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        String normalizedKeyword = keyword == null ? "" : keyword.trim();

        Page<Sets> setsPage = setRepo.searchSetsForCms(normalizedKeyword, deleted, pageable);
        Page<SetsResponse> responsePage = setsPage.map(setsMapper::toSetsResponse);

        return PageResponse.from(responsePage);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SETS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SET_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEM_DETAIL, allEntries = true)
    })
    public SetsResponse create(SetsRequest request) {
        String normalizedName = normalizeName(request.getName());

        validateDuplicateName(normalizedName);

        Sets sets = setsMapper.toEntity(request);
        sets.setName(normalizedName);
        sets.setCode(generateUniqueSetCode(normalizedName, null));
        Sets savedSets = setRepo.save(sets);

        notificationService.createAndBroadcast(
                NotificationCreateCommand.builder()
                        .type(NotificationType.SET_CREATED)
                        .title("Tạo mùa giải mới")
                        .content("Set " + savedSets.getName() + " vừa được tạo")
                        .targetType(NotificationTargetType.SETS)
                        .targetId(savedSets.getId())
                        .createdBy(1L)
                        .build()
        );
        return setsMapper.toSetsResponse(savedSets);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SETS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SET_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEM_DETAIL, allEntries = true)
    })
    public SetsResponse update(Long id, SetsRequest request) {
        Sets existingSet = getById(id);
        String normalizedName = normalizeName(request.getName());

        validateDuplicateNameForUpdate(normalizedName, id);

        setsMapper.updateEntity(request, existingSet);
        existingSet.setName(normalizedName);
        if (!StringUtils.hasText(existingSet.getCode())) {
            existingSet.setCode(generateUniqueSetCode(normalizedName, id));
        }

        Sets updatedSet = setRepo.save(existingSet);
        return setsMapper.toSetsResponse(updatedSet);
    }
    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SETS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SET_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEM_DETAIL, allEntries = true)
    })
    public void delete(Long id) {
        Sets sets = getById(id);
        if (sets.isDeleted()) {
            throw new ConflictException(
                    ErrorCode.ALREADY_DELETED,
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                    String.valueOf(id)
            );
        }
        sets.setDeleted(true);
        setRepo.save(sets);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SETS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_SET_DETAIL, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEMS, allEntries = true),
            @CacheEvict(cacheNames = CacheNames.PUBLIC_ITEM_DETAIL, allEntries = true)
    })
    public void deletedMany(List<Long> ids) {
        List<Sets> setsList = setRepo.findAllByIdInAndDeletedFalse(ids);

        if (setsList.size() != ids.size()) {
            throw new DataException(
                    ErrorCode.INCOMPLETE_DATA,
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS)
            );
        }

        setsList.forEach(s -> s.setDeleted(true));
        setRepo.saveAll(setsList);
    }

    private Sets getById(Long id) {
        return setRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                        MessageUtils.getMessage(Constants.MessageKey.FIELD_ID),
                        String.valueOf(id)
                ));
    }

    private void validateDuplicateName(String name) {
        if (setRepo.existsByName(name)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_SETS_NAME),
                    name
            );
        }
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String generateUniqueSetCode(String name, Long excludeId) {
        String baseCode = deriveSetCodeFromName(name);
        String candidate = baseCode;
        int suffix = 2;

        while (isCodeInUse(candidate, excludeId)) {
            String suffixText = "-" + suffix++;
            int maxBaseLength = Math.max(1, 100 - suffixText.length());
            String shortenedBase = baseCode.length() > maxBaseLength
                    ? baseCode.substring(0, maxBaseLength)
                    : baseCode;
            candidate = shortenedBase + suffixText;
        }

        return candidate;
    }

    private boolean isCodeInUse(String code, Long excludeId) {
        if (excludeId == null) {
            return setRepo.existsByCodeIgnoreCase(code);
        }
        return setRepo.existsByCodeIgnoreCaseAndIdNot(code, excludeId);
    }

    private String deriveSetCodeFromName(String name) {
        if (!StringUtils.hasText(name)) {
            return "set";
        }

        String normalized = Normalizer.normalize(name.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");

        if (!StringUtils.hasText(normalized)) {
            return "set";
        }

        return normalized.length() > 100 ? normalized.substring(0, 100) : normalized;
    }

    private void validateDuplicateNameForUpdate(String name, Long id) {
        if (setRepo.existsByNameAndIdNot(name, id)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_SETS_NAME),
                    name
            );
        }
    }

    private SetOptionResponse toSetOption(Sets set) {
        return SetOptionResponse.builder()
                .id(set.getId())
                .code(set.getCode())
                .name(set.getName())
                .build();
    }
}
