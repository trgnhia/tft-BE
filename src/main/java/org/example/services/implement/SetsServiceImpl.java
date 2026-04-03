package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.constant.Constants;
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
@Transactional(readOnly = true )
public class SetsServiceImpl implements SetsService {
    private final SetsRepository setRepo;
    private final SetsMapper setsMapper;
    @Override
    public SetsResponse getSetById(Long id) {
        Sets sets = setRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        MessageUtils.getMessage(Constants.MessageKey.ENTITY_SETS),
                        new Object[]{Constants.MessageKey.ENTITY_CHAMP}));
        return setsMapper.toSetsResponse(sets);
    }

    @Override
    public List<SetsResponse> getAllSet() {
        List<Sets> sets = setRepo.findAll();
        return setsMapper.toListSetsResponse(sets);
    }

    @Override
    @Transactional
    public SetsResponse create(SetsRequest request) {
       if (setRepo.existsByName(request.getName().trim())) {
           throw new ConflictException("Set name already exists");
       }

       Sets sets = new Sets();
       sets.setName(request.getName().trim());
       sets.setActive(request.getIsActive() != null ? request.getIsActive() : true);
       Sets savedSets = setRepo.save(sets);
       return setsMapper.toSetsResponse(savedSets);
    }

    @Override
    @Transactional
    public SetsResponse update(Long id, SetsRequest request) {
        Sets existingSet = setRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Set not found", new Object[]{Constants.MessageKey.ENTITY_CHAMP}));
        String normalizedName = request.getName().trim();

        if (!existingSet.getName().equalsIgnoreCase(normalizedName)
                && setRepo.existsByName(normalizedName)) {
            throw new ConflictException("Set name already exists");
        }

        existingSet.setName(normalizedName);
        if (request.getIsActive() != null) {
            existingSet.setActive(request.getIsActive());
        }
        Sets updatedSet = setRepo.save(existingSet);
        return setsMapper.toSetsResponse(updatedSet);
    }
}
