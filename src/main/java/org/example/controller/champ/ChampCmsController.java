package org.example.controller.champ;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.champs.*;
import org.example.dto.upload.DeleteUploadRequest;
import org.example.dto.upload.FileUploadResponse;
import org.example.services.FileStorageService;
import org.example.services.implement.ChampServiceImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/cms/champs")
@RequiredArgsConstructor
@Slf4j
public class ChampCmsController {

    private final ChampServiceImpl champService;
    private final FileStorageService fileStorageService;

    @PostMapping
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<ChampResponse>> create(
            @RequestBody @Valid CreateChampRequest request) {
        log.info("REST request to create Champ: {}", request.getSlug());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(champService.create(request)));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<ChampResponse>>> bulkCreate(
            @RequestBody @Valid BulkCreateRequest request) {
        log.info("REST bulkCreate champs count={}", request.getChamps().size());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(champService.bulkCreate(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<ChampResponse> update(
            @PathVariable Long id,
            @RequestBody @Valid UpdateChampRequest request) {
        log.info("REST request to update Champ id: {}", id);
        return ApiResponse.success(champService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        log.info("REST request to delete Champ id: {}", id);
        champService.delete(id);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/bulk")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<Void> bulkDelete(@RequestBody @Valid BulkDeleteRequest request) {
        log.info("REST bulkDelete champs ids={}", request.getIds());
        champService.bulkDelete(request);
        return ApiResponse.success(null);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<PageResponse<ChampResponse>> search(
            @ModelAttribute ChampFilterRequest filter,
            Pageable pageable) {
        log.info("REST CMS search champs filter={}", filter);
        return ApiResponse.success(champService.search(filter, pageable));
    }

    @GetMapping("/dropdown")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<List<ChampResponse>> getForDropdown(
            @RequestParam(required = false) Long setId) {
        log.info("REST CMS champ dropdown setId={}", setId);
        return ApiResponse.success(champService.getAllSortedByNameAsc(setId));
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ChampResponse>> getAllAdmin(
            @RequestParam(required = false) String keyword,
            Pageable pageable) {
        log.info("REST request for ADMIN to get all Champs, keyword: {}", keyword);
        return ApiResponse.success(champService.getAllAdmin(keyword, pageable));
    }

    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChampResponse> getByIdAdmin(@PathVariable Long id) {
        log.info("REST request for ADMIN to get Champ id: {}", id);
        return ApiResponse.success(champService.getByIdAdmin(id));
    }

    @GetMapping("/admin/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PageResponse<ChampResponse>> searchAdmin(
            @ModelAttribute ChampFilterRequest filter,
            Pageable pageable) {
        log.info("REST ADMIN search champs filter={}", filter);
        return ApiResponse.success(champService.searchAdmin(filter, pageable));
    }

    @GetMapping("/admin/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<ChampOverviewStatsResponse> getStats() {
        log.info("REST ADMIN get champ stats");
        return ApiResponse.success(champService.getStats());
    }

    @PatchMapping("/admin/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> restore(@PathVariable Long id) {
        log.info("REST request to restore Champ id: {}", id);
        champService.restore(id);
        return ApiResponse.success(null);
    }

    @PatchMapping("/admin/bulk/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BulkRestoreChampResponse> bulkRestore(@RequestBody @Valid BulkDeleteRequest request) {
        log.info("REST ADMIN bulkRestore champs ids={}", request.getIds());
        return ApiResponse.success(champService.bulkRestore(request));
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ResponseEntity<ApiResponse<FileUploadResponse>> uploadImage(
            @RequestParam("file") MultipartFile file) {
        log.info("REST request to upload champ image: {}", file.getOriginalFilename());
        FileUploadResponse response = fileStorageService.storeImage(file, "champs");
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/upload-image")
    @PreAuthorize("hasAnyRole('EDITOR', 'ADMIN')")
    public ApiResponse<Void> deleteUploadedImage(@RequestBody @Valid DeleteUploadRequest request) {
        log.info("REST request to delete champ uploaded image: {}", request.getUrl());
        fileStorageService.deleteImageByUrl(request.getUrl());
        return ApiResponse.success(null);
    }
}
