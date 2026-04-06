package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.trait.CreateTraitRequest;
import org.example.dto.trait.TraitResponse;
import org.example.dto.trait.UpdateTraitRequest;
import org.springframework.data.domain.Pageable;

public interface TraitService {
    PageResponse<TraitResponse> getAll(String keyword, Pageable pageable);
    TraitResponse getById(Long id);
    TraitResponse getBySlug(String slug);
    TraitResponse create(CreateTraitRequest request);
    TraitResponse update(Long id, UpdateTraitRequest request);
    void delete(Long id);

    // admin
    PageResponse<TraitResponse> getAllAdmin(String keyword, Pageable pageable);
    TraitResponse getByIdAdmin(Long id);
    void restore(Long id);
}