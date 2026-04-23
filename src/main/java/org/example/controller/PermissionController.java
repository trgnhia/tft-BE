package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.dto.permission.CreatePermissionRequest;
import org.example.dto.permission.PermissionOptions;
import org.example.dto.permission.UpdatePermissionRequest;
import org.example.services.PermissionService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<ApiResponse<Object>> getPermissions(@RequestParam(required = false) String keyword, Pageable pageable) {
        var result = permissionService.getPermissions(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Object>> createPermission(@RequestBody @Valid CreatePermissionRequest request) {
        var result = permissionService.create(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> getPermissionDetail(@PathVariable Long id) {
        var result = permissionService.getPermissionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> updatePermission(@PathVariable Long id, @RequestBody @Valid UpdatePermissionRequest request) {
        var result = permissionService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Object>> deletePermission(@PathVariable Long id) {
        permissionService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Delete successfully"));
    }

    @GetMapping("/options")
    public ResponseEntity<ApiResponse<PermissionOptions>> getPermissionOptions() {
        var response = permissionService.getOptions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
