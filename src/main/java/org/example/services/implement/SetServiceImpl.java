package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.set.SetRequest;
import org.example.dto.set.SetResponse;
import org.example.entities.Set;
import org.example.mapper.SetMapper;
import org.example.repositories.SetRepository;
import org.example.services.SetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true )
public class SetServiceImpl implements SetService {
    private final SetRepository setRepo;
    private final SetMapper setMapper;
    @Override
    public SetResponse getSetById(Long id) {
        Set set = setRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
        return setMapper.toSetResponse(set);
    }

    @Override
    public List<SetResponse> getAllSet() {
        return List.of();
    }

    @Override
    @Transactional
    public SetResponse create(SetRequest request) {
       if (setRepo.existsByName(request.getName().trim())) {
           throw new ConflictException("Set name already exists");
       }

       Set set = new Set();
       set.setName(request.getName().trim());
       set.setActive(request.getIsActive() != null ? request.getIsActive() : true);
       Set savedSet = setRepo.save(set);
       return setMapper.toSetResponse(savedSet);
    }

    @Override
    @Transactional
    public SetResponse update(Long id, SetRequest request) {
        return null;
    }
}
