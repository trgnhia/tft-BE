package org.example.dto.role;

import jakarta.validation.constraints.Size;
import org.example.dto.user.PermissionDto;

import java.util.Set;

public record UpdateRolePermissionRequest(
        @Size(message = "{error.PERMISSION_MIN}")
        Set<PermissionDto> permissions) {
}
