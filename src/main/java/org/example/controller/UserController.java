package org.example.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.annotations.RequirePermission;
import org.example.common.constant.Constants;
import org.example.common.enums.PERMISSION;
import org.example.common.enums.RESOURCE;
import org.example.core.api.ApiResponse;
import org.example.core.api.PageResponse;
import org.example.dto.user.*;
import org.example.imports.model.ImportExecutionResult;
import org.example.imports.service.GenericImportService;
import org.example.security.SecurityUser;
import org.example.services.UserImportPersistenceService;
import org.example.services.UserService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.example.util.MessageUtils.getMessage;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;
    private final GenericImportService genericImportService;
    private final UserImportPersistenceService userImportPersistenceService;


    @GetMapping("/my-info")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMyProfile(@AuthenticationPrincipal SecurityUser securityUser) {
        var user = userService.getMyInfo(securityUser.getId());
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.READ)
    public ResponseEntity<PageResponse<UserResponse>> getAllUser(@ParameterObject UserFilter userFilter,
                                                                 @ParameterObject @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var userPages = userService.getAllUser(userFilter, pageable);
        return ResponseEntity.ok(userPages);
    }

    @GetMapping("/{userId}")
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.READ)
    public ResponseEntity<ApiResponse<UserDetailedResponse>> getDetailed(@PathVariable Long userId) {
        var user = userService.getDetailedById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.CREATE)
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody @Valid CreateUserRequest request) {
        var created = userService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success(created));
    }

    @PatchMapping("/{userId}/role")
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.UPDATE)
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(@PathVariable Long userId, @RequestBody @Valid UpdateUserRoleRequest request) {
        var saved = userService.updateUserRole(userId, request);
        return ResponseEntity.ok(ApiResponse.success(saved));
    }

    @PutMapping("/{userId}")
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.UPDATE)
    public ResponseEntity<ApiResponse<UserDetailedResponse>> updateUserProfile(@PathVariable Long userId, @RequestBody @Valid UpdateUserProfileRequest request) {
        var updated = userService.updateUserProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/{userId}")
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.DELETE)
    public ResponseEntity<ApiResponse<UserDetailedResponse>> deleteUser(@PathVariable Long userId) {
        var deleted = userService.deleteUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(deleted));
    }

    @PatchMapping("/{userId}/recover")
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.UPDATE)
    public ResponseEntity<ApiResponse<UserDetailedResponse>> recoverUser(@PathVariable Long userId) {
        var result = userService.recoverUser(userId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PatchMapping("/{userId}/reset-password")
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.UPDATE)
    public ResponseEntity<ApiResponse<Object>> resetUserPassword(@PathVariable Long userId, @RequestBody @Valid ResetUserPasswordRequest request) {
        var result = userService.resetUserPassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequirePermission(resource = RESOURCE.USER, permission = PERMISSION.IMPORT)
    public ResponseEntity<Object> importUsers(@RequestParam("file") List<MultipartFile> files) {
        if (files == null || files.size() != 1) {
            throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_SINGLE_FILE_REQUIRED));
        }
        MultipartFile file = files.get(0);
        ImportExecutionResult result = genericImportService.importFile(
                file,
                UserImportDto.class,
                userImportPersistenceService::persist
        );

        if (!result.hasFailures()) {
            return ResponseEntity.ok(ApiResponse.success(result.message()));
        }

        ByteArrayResource resource = new ByteArrayResource(result.errorFileContent());
        HttpStatus status = result.successCount() > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.BAD_REQUEST;

        return ResponseEntity.status(status)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + result.errorFileName() + "\"")
                .header("X-Import-Message", result.message())
                .contentType(MediaType.parseMediaType(result.errorFileContentType()))
                .contentLength(result.errorFileContent().length)
                .body(resource);
    }

}
