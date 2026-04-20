package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.champs.BulkDeleteRequest;
import org.example.dto.trait.BulkCreateTraitRequest;
import org.example.dto.trait.CreateTraitRequest;
import org.example.dto.trait.TraitFilterRequest;
import org.example.dto.trait.TraitOverviewStatsResponse;
import org.example.dto.trait.TraitResponse;
import org.example.dto.trait.UpdateTraitRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TraitService {
    PageResponse<TraitResponse> getAll(String keyword, Pageable pageable);
    TraitResponse getById(Long id);
    TraitResponse getBySlug(String slug);
    TraitResponse create(CreateTraitRequest request);
    TraitResponse update(Long id, UpdateTraitRequest request);
    void delete(Long id);
    List<TraitResponse> bulkCreate(BulkCreateTraitRequest request);
    void bulkDelete(BulkDeleteRequest request);
    PageResponse<TraitResponse> search(TraitFilterRequest filter, Pageable pageable);
    List<TraitResponse> getForDropdown(Long setId);
    List<TraitResponse> getBySetId(Long setId);
    List<TraitResponse> getAllSortedByNameAsc(Long setId);
    PageResponse<TraitResponse> getAllAdmin(String keyword, Pageable pageable);
    TraitResponse getByIdAdmin(Long id);
    void restore(Long id);
    PageResponse<TraitResponse> searchAdmin(TraitFilterRequest filter, Pageable pageable);
    void bulkRestore(BulkDeleteRequest request);
    TraitOverviewStatsResponse getStats();
}
