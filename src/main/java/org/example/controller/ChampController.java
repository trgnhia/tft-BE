package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.*;
import org.example.services.ChampService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping
@RequiredArgsConstructor
@Slf4j
public class ChampController {
    private final ChampService champService;

    // public api /api/v1/champs
    @GetMapping("/champs")
    public ApiResponse<PageResponse<ChampResponse>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("REST request to get all Champs with keyword: {}", keyword);
        return ApiResponse.success(champService.getAll(keyword, pageable));
    }

    @GetMapping("/champs/{id}")
    public ApiResponse<ChampResponse> getById(@PathVariable Long id) {
        log.info("REST request to get Champ by id: {}", id);
        return ApiResponse.success(champService.getById(id));
    }

    @GetMapping("/champs/slug/{slug}")
    public ApiResponse<ChampResponse> getBySlug(@PathVariable String slug) {
        log.info("REST request to get Champ by slug: {}", slug);
        return ApiResponse.success(champService.getBySlug(slug));
    }

    @GetMapping("/champs/set/{setId}")
    public ApiResponse<List<ChampResponse>> getBySetId(@PathVariable Long setId) {
        log.info("REST get champs by setId={}", setId);
        return ApiResponse.success(champService.getBySetId(setId));
    }

    //editor them, sua, xoa
    @PostMapping("/cms/champs")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChampResponse>> create(
            @RequestBody @Valid CreateChampRequest request) {
        log.info("REST request to create Champ: {}", request.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(champService.create(request)));
    }

    @PostMapping("/cms/champs/bulk")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ChampResponse>>> bulkCreate(
            @RequestBody @Valid BulkCreateRequest request) {
        log.info("REST bulkCreate champs count={}", request.getChamps().size());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(champService.bulkCreate(request)));
    }

    @PutMapping("/cms/champs/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<ChampResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateChampRequest request) {
        log.info("REST request to update Champ id: {}", id);
        return ApiResponse.success(champService.update(id, request));
    }

    @DeleteMapping("/cms/champs/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete Champ id: {}", id);
        champService.delete(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/cms/champs/bulk")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<Void> bulkDelete(@RequestBody @Valid BulkDeleteRequest request) {
        log.info("REST bulkDelete champs ids={}", request.getIds());
        champService.bulkDelete(request);
        return ApiResponse.success(null);
    }

    //admin xem tất cả
    @GetMapping("/cms/admin/champs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ChampResponse>> getAllAdmin(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("REST request for ADMIN to get all Champs, keyword: {}", keyword);
        return ApiResponse.success(champService.getAllAdmin(keyword, pageable));
    }

    @GetMapping("/cms/admin/champs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChampResponse> getByIdAdmin(@PathVariable Long id) {
        log.info("REST request for ADMIN to get Champ id: {}", id);
        return ApiResponse.success(champService.getByIdAdmin(id));
    }

    @GetMapping("/cms/admin/champs/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ChampResponse>> searchAdmin(
            @ModelAttribute ChampFilterRequest filter,
            Pageable pageable) {
        log.info("REST ADMIN search champs filter={}", filter);
        return ApiResponse.success(champService.searchAdmin(filter, pageable));
    }

    @GetMapping("/cms/admin/champs/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChampOverviewStatsResponse> getStats() {
        log.info("REST ADMIN get champ stats");
        return ApiResponse.success(champService.getStats());
    }

    @PatchMapping("/cms/admin/champs/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        log.info("REST request to restore Champ id: {}", id);
        champService.restore(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/cms/admin/champs/bulk/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> bulkRestore(@RequestBody @Valid BulkDeleteRequest request) {
        log.info("REST ADMIN bulkRestore champs ids={}", request.getIds());
        champService.bulkRestore(request);
        return ApiResponse.success(null);
    }
}
