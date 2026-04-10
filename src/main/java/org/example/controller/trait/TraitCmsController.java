package org.example.controller.trait;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.BulkDeleteRequest;
import org.example.dto.trait.*;
import org.example.services.TraitService;
import org.example.services.implement.TraitServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cms/traits")
@RequiredArgsConstructor
@Slf4j
public class TraitCmsController {

    private final TraitServiceImpl traitService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<TraitResponse>> create(
            @RequestBody @Valid CreateTraitRequest request) {
        log.info("[POST] /cms/traits slug={}", request.getSlug());
        TraitResponse created = traitService.create(request);
        log.info("[POST] /cms/traits created id={}", created.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<TraitResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateTraitRequest request) {
        log.info("[PUT] /cms/traits/{}", id);
        return ApiResponse.success(traitService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("[DELETE] /cms/traits/{}", id);
        traitService.delete(id);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<PageResponse<TraitResponse>> search(
            @ModelAttribute TraitFilterRequest filter,
            Pageable pageable) {
        log.info("[GET] /cms/traits/search filter={}", filter);
        return ApiResponse.success(traitService.search(filter, pageable));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<TraitResponse>> getAllAdmin(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("[GET] /cms/traits/admin keyword={} page={} size={}",
                keyword, pageable.getPageNumber(), pageable.getPageSize());
        return ApiResponse.success(traitService.getAllAdmin(keyword, pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TraitResponse> getByIdAdmin(@PathVariable Long id) {
        log.info("[GET] /cms/traits/admin/{}", id);
        return ApiResponse.success(traitService.getByIdAdmin(id));
    }

    @PatchMapping("/admin/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        log.info("[PATCH] /cms/traits/admin/{}/restore", id);
        traitService.restore(id);
        return ApiResponse.success(null);
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<TraitOverviewStatsResponse> getStats() {
        log.info("[GET] /cms/traits/admin/stats");
        return ApiResponse.success(traitService.getStats());
    }

    @PatchMapping("/admin/bulk-restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> bulkRestore(@RequestBody @Valid BulkDeleteRequest request) {
        log.info("[PATCH] /cms/traits/admin/bulk-restore count={}", request.getIds().size());
        traitService.bulkRestore(request);
        return ApiResponse.success(null);
    }
}