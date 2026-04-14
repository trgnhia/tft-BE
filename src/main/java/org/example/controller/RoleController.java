package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.role.CreateRoleRequest;
import org.example.dto.role.RoleDto;
import org.example.dto.role.UpdateRolePermissionRequest;
import org.example.dto.role.UpdateRoleRequest;
import org.example.services.RoleService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
@PreAuthorize("hasRole('ADMIN')")
public class RoleController {
    private final RoleService roleService;

    @GetMapping
    public ResponseEntity<PageResponse<RoleDto>> getAll(@RequestParam(required = false) String keyword,
                                                        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        var result = roleService.getAll(keyword, pageable);
        return ResponseEntity.ok(PageResponse.from(result));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<RoleDto>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        var result = roleService.createRole(request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> getById(@PathVariable Long id) {
        var result = roleService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> updateRole(@PathVariable Long id, @Valid @RequestBody UpdateRoleRequest request) {
        var result = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{id}/permissions")
    public ResponseEntity<ApiResponse<RoleDto>> updateRolePermissions(@PathVariable Long id, @Valid @RequestBody UpdateRolePermissionRequest request) {
        var result = roleService.updateRolePermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<RoleDto>> deleteRole(@PathVariable Long id) {
        var result = roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
