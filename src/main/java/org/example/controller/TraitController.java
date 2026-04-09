package org.example.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.BulkDeleteRequest;
import org.example.dto.trait.*;
import org.example.services.implement.TraitServiceImpl;
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
public class TraitController {
    private final TraitServiceImpl traitService;

    @GetMapping("/traits")
    public ApiResponse<PageResponse<TraitResponse>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("[GET] /traits keyword={} page={} size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        return ApiResponse.success(traitService.getAll(keyword, pageable));
    }

    @GetMapping("/traits/{id}")
    public ApiResponse<TraitResponse> getById(@PathVariable Long id) {
        log.info("[GET] /traits/{}", id);
        return ApiResponse.success(traitService.getById(id));
    }

    @GetMapping("/traits/slug/{slug}")
    public ApiResponse<TraitResponse> getBySlug(@PathVariable String slug) {
        log.info("[GET] /traits/slug/{}", slug);
        return ApiResponse.success(traitService.getBySlug(slug));
    }

    // ── Editor ───────────────────────────────────────────
    @PostMapping("/editor/traits")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<TraitResponse>> create(
            @RequestBody @Valid CreateTraitRequest request) {
        log.info("[POST] /editor/traits slug={}", request.getSlug());
        TraitResponse created = traitService.create(request);
        log.info("[POST] /editor/traits created id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/editor/traits/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<TraitResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTraitRequest request) {
        log.info("[PUT] /editor/traits/{}", id);
        return ApiResponse.success(traitService.update(id, request));
    }

    @DeleteMapping("/editor/traits/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("[DELETE] /editor/traits/{}", id);
        traitService.delete(id);
    }

    // ── Admin ─────────────────────────────────────────────
    @GetMapping("/admin/traits")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<TraitResponse>> getAllAdmin(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("[GET] /admin/traits keyword={} page={} size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        return ApiResponse.success(traitService.getAllAdmin(keyword, pageable));
    }

    @GetMapping("/admin/traits/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TraitResponse> getByIdAdmin(@PathVariable Long id) {
        log.info("[GET] /admin/traits/{}", id);
        return ApiResponse.success(traitService.getByIdAdmin(id));
    }

    @PatchMapping("/admin/traits/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        log.info("[PATCH] /admin/traits/{}/restore", id);
        traitService.restore(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/traits/search")
    public ApiResponse<PageResponse<TraitResponse>> search(
            @ModelAttribute TraitFilterRequest filter, // @ModelAttribute dùng để map Query Params vào Object
            Pageable pageable) {
        log.info("[GET] /traits/search filter={}", filter);
        return ApiResponse.success(traitService.search(filter, pageable));
    }

    @GetMapping("/admin/traits/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TraitOverviewStatsResponse> getStats() {
        log.info("[GET] /admin/traits/stats");
        return ApiResponse.success(traitService.getStats());
    }

    @PatchMapping("/admin/traits/bulk-restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> bulkRestore(@RequestBody BulkDeleteRequest request) {
        log.info("[PATCH] /admin/traits/bulk-restore count={}", request.getIds().size());
        traitService.bulkRestore(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/traits/dropdown")
    public ApiResponse<List<TraitResponse>> getForDropdown() {
        log.info("[GET] /traits/dropdown");
        return ApiResponse.success(traitService.getForDropdown());
    }
}
