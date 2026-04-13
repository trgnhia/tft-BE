package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.champs.BulkDeleteRequest;
import org.example.dto.trait.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TraitService {
    PageResponse<TraitResponse> getAll(String keyword, Pageable pageable);
    TraitResponse getById(Long id);
    TraitResponse getBySlug(String slug);
    TraitResponse create(CreateTraitRequest request);
    TraitResponse update(Long id, UpdateTraitRequest request);
    void delete(Long id);

    PageResponse<TraitResponse> search(TraitFilterRequest filter, Pageable pageable);

    /** Dùng cho dropdown / select box */
    List<TraitResponse> getForDropdown();

    // admin
    PageResponse<TraitResponse> getAllAdmin(String keyword, Pageable pageable);
    TraitResponse getByIdAdmin(Long id);
    void restore(Long id);

    /** Search nâng cao phía admin (bao gồm cả deleted) */
    PageResponse<TraitResponse> searchAdmin(TraitFilterRequest filter, Pageable pageable);

    /** Restore nhiều trait cùng lúc */
    void bulkRestore(BulkDeleteRequest request);

    /** Thống kê tổng quan */
    TraitOverviewStatsResponse getStats();
}