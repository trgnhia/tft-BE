package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.user.CreateUserRequest;
import org.example.dto.user.UpdateUserRoleRequest;
import org.example.dto.user.UserFilter;
import org.example.dto.user.UserResponse;
import org.example.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@PreAuthorize("hasAnyRole(['ADMIN','EDITOR'])")
public class UserController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUser(@RequestParam UserFilter userFilter, @ParameterObject @PageableDefault() Pageable pageable) {
        var users = userService.getAllUser(pageable);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        var created = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PatchMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable Long userId, @RequestBody @Valid UpdateUserRoleRequest request) {
        var saved = userService.updateUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }
}
