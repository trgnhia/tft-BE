package org.example.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateRoleRequest(
        @NotNull @NotBlank String code,
        @NotNull @NotBlank String name,
        @NotNull @NotBlank String description) {
}
