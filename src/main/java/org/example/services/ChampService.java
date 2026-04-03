package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.champs.ChampResponse;
import org.example.dto.champs.CreateChampRequest;
import org.example.dto.champs.UpdateChampRequest;
import org.springframework.data.domain.Pageable;

public interface ChampService {
    // user
    PageResponse<ChampResponse> getAll(String keyword, Pageable pageable);
    ChampResponse getById(Long id);
    ChampResponse getBySlug(String slug);

    // editor
    ChampResponse create(CreateChampRequest request);
    ChampResponse update(Long id, UpdateChampRequest request);
    void delete(Long id);


    // admin
    PageResponse<ChampResponse> getAllAdmin(String keyword, Pageable pageable);
    ChampResponse getByIdAdmin(Long id);
    void restore(Long id);
}
