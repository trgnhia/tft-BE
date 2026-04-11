package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.user.*;
import org.example.security.SecurityUser;
import org.example.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@PreAuthorize("hasAnyRole('ADMIN','EDITOR','USER')")
public class UserController {
    private final UserService userService;


    @GetMapping("/my-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        var user = userService.getMyInfo(securityUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUser(@ParameterObject UserFilter userFilter,
                                                                 @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var userPages = userService.getAllUser(userFilter, pageable);
        return ResponseEntity.ok(userPages);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('USER_READ')")
    public ResponseEntity<ApiResponse<UserDetailedResponse>> getDetailed(@PathVariable Long userId) {
        var user = userService.getDetailedById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
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

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailedResponse>> updateUserProfile(@PathVariable Long userId, @RequestBody @Valid UpdateUserProfileRequest request) {
        var updated = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailedResponse>> deleteUser(@PathVariable Long userId) {
        var deleted = userService.deleteUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(deleted));
    }

    @PatchMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserDetailedResponse>> updateStatus(@PathVariable Long userId, @RequestBody @Valid UpdateAccountStatusRequest request) {
        var result = userService.updateStatus(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

}
