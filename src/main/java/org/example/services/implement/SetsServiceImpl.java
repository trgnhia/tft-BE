package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.entities.Sets;

import org.example.mapper.SetsMapper;
import org.example.repositories.SetsRepository;
import org.example.services.SetsService;
import org.example.util.MessageUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetsServiceImpl implements SetsService {

    private final SetsRepository setRepo;
    private final SetsMapper setsMapper;

    @Override
    public SetsResponse getSetById(Long id) {
        Sets sets = getById(id);
        return setsMapper.toSetsResponse(sets);
    }

    @Override
    public List<SetsResponse> getAllPublishedSet() {
        List<Sets> sets = setRepo.findAllByDeletedFalse();
        return setsMapper.toListSetsResponse(sets);
    }

    @Override
    public List<SetsResponse> getAllSet() {
        List<Sets> sets = setRepo.findAll();
        return setsMapper.toListSetsResponse(sets);
    }

    @Override
    @Transactional
    public SetsResponse create(SetsRequest request) {
        String normalizedName = normalizeName(request.getName());

        validateDuplicateName(normalizedName);

        Sets sets = setsMapper.toEntity(request);
        sets.setName(normalizedName);
        sets.setActive(request.getIsActive() == null || request.getIsActive());
        Sets savedSets = setRepo.save(sets);
        return setsMapper.toSetsResponse(savedSets);
    }

    @Override
    @Transactional
    public SetsResponse update(Long id, SetsRequest request) {
        Sets existingSet = getById(id);
        String normalizedName = normalizeName(request.getName());

        validateDuplicateNameForUpdate(normalizedName, id);

        existingSet.setName(normalizedName);
        if (request.getIsActive() != null) {
            existingSet.setActive(request.getIsActive());
        }

        Sets updatedSet = setRepo.save(existingSet);
        return setsMapper.toSetsResponse(updatedSet);
    }

    @Override
    @Transactional
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


    private void validateDuplicateNameForUpdate(String name, Long id) {
        if (setRepo.existsByNameAndIdNot(name, id)) {
            throw new ConflictException(
                    MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                    MessageUtils.getMessage(Constants.MessageKey.FIELD_SETS_NAME),
                    name
            );
        }
    }
}