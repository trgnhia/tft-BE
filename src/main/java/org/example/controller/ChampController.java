package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.ChampResponse;
import org.example.dto.champs.CreateChampRequest;
import org.example.dto.champs.UpdateChampRequest;
import org.example.services.ChampService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ChampController {
    private final ChampService champService;

    // public api /api/v1/champs
    @GetMapping("/champs")
    public ApiResponse<PageResponse<ChampResponse>> getAll(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ApiResponse.success(champService.getAll(keyword, pageable));
    }

    @GetMapping("/champs/{id}")
    public ApiResponse<ChampResponse> getById(@PathVariable Long id) {
        return ApiResponse.success(champService.getById(id));
    }

    @GetMapping("/champs/slug/{slug}")
    public ApiResponse<ChampResponse> getBySlug(@PathVariable String slug) {
        return ApiResponse.success(champService.getBySlug(slug));
    }

    //editor them, sua, xoa
    @PostMapping("/editor/champs")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChampResponse>> create(
            @RequestBody @Valid CreateChampRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(champService.create(request)));
    }

    @PutMapping("/editor/champs/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<ChampResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateChampRequest request) {
        return ApiResponse.success(champService.update(id, request));
    }

    @DeleteMapping("/editor/champs/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        champService.delete(id);
        return ApiResponse.success(null);
    }

    //admin xem tất cả
    @GetMapping("/admin/champs")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ChampResponse>> getAllAdmin(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        return ApiResponse.success(champService.getAllAdmin(keyword, pageable));
    }

    @GetMapping("/admin/champs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChampResponse> getByIdAdmin(@PathVariable Long id) {
        return ApiResponse.success(champService.getByIdAdmin(id));
    }

    @PatchMapping("/admin/champs/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        champService.restore(id);
        return ApiResponse.success(null);
    }
}
