package org.example.dto.permission;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreatePermissionRequest(@NotNull @NotBlank String resource, @NotNull @NotBlank String permission,
                                      @NotNull @NotBlank String name,
                                      @NotNull @NotBlank String description) {
}
