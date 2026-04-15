package org.example.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.example.dto.user.PermissionDto;

import java.util.Set;

public record CreateRoleRequest(
        @NotNull @NotBlank String code,
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String description,
        @Size(min = 1, message = "{error.PERMISSION_MIN}")
        Set<PermissionDto> permissions) {
}
