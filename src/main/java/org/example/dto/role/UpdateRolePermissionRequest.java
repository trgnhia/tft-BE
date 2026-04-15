package org.example.dto.role;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record UpdateRolePermissionRequest(
        @Size(message = "{error.PERMISSION_MIN}")
        Set<Long> permissionIds) {
}
