package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.champs.*;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ChampService {
    // public
    PageResponse<ChampResponse> getAll(String keyword, Pageable pageable);
    ChampResponse getById(Long id);
    ChampResponse getBySlug(String slug);

    PageResponse<ChampResponse> search(ChampFilterRequest filter, Pageable pageable);
    List<ChampResponse> getBySetId(Long setId); // dung cho dropdown
    List<ChampResponse> getAllSortedByNameAsc(Long setId);

    // editor
    ChampResponse create(CreateChampRequest request);
    ChampResponse update(Long id, UpdateChampRequest request);
    void delete(Long id);

    List<ChampResponse> bulkCreate(BulkCreateRequest request);
    void bulkDelete(BulkDeleteRequest request);


    // admin
    PageResponse<ChampResponse> getAllAdmin(String keyword, Pageable pageable);
    ChampResponse getByIdAdmin(Long id);
    void restore(Long id);

    /** Search nâng cao phía admin (bao gồm cả deleted) */
    PageResponse<ChampResponse> searchAdmin(ChampFilterRequest filter, Pageable pageable);

    /** Restore nhiều champ cùng lúc */
    BulkRestoreChampResponse bulkRestore(BulkDeleteRequest request);

    /** Thống kê tổng quan */
    ChampOverviewStatsResponse getStats();
}
