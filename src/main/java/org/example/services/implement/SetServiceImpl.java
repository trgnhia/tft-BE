package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.set.SetRequest;
import org.example.dto.set.SetResponse;
import org.example.entities.Set;

import org.example.repositories.SetRepository;
import org.example.services.SetService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

//import java.util.List;
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true )
//public class SetServiceImpl implements SetService {
//    private final SetRepository setRepo;
//    private final SetMapper setMapper;
//    @Override
//    public SetResponse getSetById(Long id) {
//        Set set = setRepo.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
//        return setMapper.toSetResponse(set);
//    }
//
//    @Override
//    public List<SetResponse> getAllSet() {
//        return List.of();
//    }
//
//    @Override
//    @Transactional
//    public SetResponse create(SetRequest request) {
//       if (setRepo.existsByName(request.getName().trim())) {
//           throw new ConflictException("Set name already exists");
//       }
//
//       Set set = new Set();
//       set.setName(request.getName().trim());
//       set.setActive(request.getIsActive() != null ? request.getIsActive() : true);
//       Set savedSet = setRepo.save(set);
//       return setMapper.toSetResponse(savedSet);
//    }
//
//    @Override
//    @Transactional
//    public SetResponse update(Long id, SetRequest request) {
//        return null;
//    }
//}


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetServiceImpl implements SetService {

    private final SetRepository setRepo;

    @Override
    public SetResponse getSetById(Long id) {
        Set set = findSetById(id);
        return toResponse(set);
    }

    @Override
    public List<SetResponse> getAllSet() {
        return setRepo.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public SetResponse create(SetRequest request) {
        String name = normalizeName(request.getName());

        if (setRepo.existsByName(name)) {
            throw new ConflictException("Set name already exists");
        }

        Set set = new Set();
        set.setName(name);
        set.setActive(request.getIsActive() != null ? request.getIsActive() : true);

        Set savedSet = setRepo.save(set);
        return toResponse(savedSet);
    }

    @Override
    @Transactional
    public SetResponse update(Long id, SetRequest request) {
        return null;
    }

    private Set findSetById(Long id) {
        return setRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private SetResponse toResponse(Set set) {
        SetResponse response = new SetResponse();
        response.setId(set.getId());
        response.setName(set.getName());
        response.setIsActive(set.isActive());
        response.setCreatedAt(set.getCreatedAt());
        response.setUpdatedAt(set.getUpdatedAt());
        response.setCreatedByName(set.getCreatedBy() != null ? String.valueOf(set.getCreatedBy()) : null);
        return response;
    }
}
