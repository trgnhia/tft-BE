package org.example.services.implement;
import lombok.RequiredArgsConstructor;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.dto.sets.SetsRequest;
import org.example.dto.sets.SetsResponse;
import org.example.entities.Sets;

import org.example.mapper.SetsMapper;
import org.example.repositories.SetsRepository;
import org.example.services.SetsService;
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
                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
        return setsMapper.toSetsResponse(sets);
    }

    @Override
    public List<SetsResponse> getAllSet() {
        return List.of();
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
        return null;
    }
}


//package org.example.services.implement;
//
//import lombok.RequiredArgsConstructor;
//import org.example.common.exception.ConflictException;
//import org.example.common.exception.ResourceNotFoundException;
//import org.example.dto.set.SetRequest;
//import org.example.dto.set.SetResponse;
//import org.example.entities.Sets;
//import org.example.repositories.SetsRepository;
//import org.example.services.SetsService;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class SetsServiceImpl implements SetsService {
//
//    private final SetsRepository setRepo;
//
//    @Override
//    public SetResponse getSetById(Long id) {
//        Sets sets = setRepo.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Set not found"));
//
//        return SetResponse.builder()
//                .id(sets.getId())
//                .name(sets.getName())
//                .isActive(sets.isActive())
//                .build();
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
//        String name = request.getName().trim();
//
//        if (setRepo.existsByName(name)) {
//            throw new ConflictException("Set name already exists");
//        }
//
//        Sets sets = new Sets();
//        sets.setName(name);
//        sets.setActive(request.getIsActive() != null ? request.getIsActive() : true);
//
//        Sets savedSets = setRepo.save(sets);
//
//        return SetResponse.builder()
//                .id(savedSets.getId())
//                .name(savedSets.getName())
//                .isActive(savedSets.isActive())
//                .build();
//    }
//
//    @Override
//    public SetResponse update(Long id, SetRequest request) {
//        return null;
//    }
//}