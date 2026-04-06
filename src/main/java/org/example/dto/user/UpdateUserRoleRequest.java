package org.example.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateUserRoleRequest(@NotNull(message = "{error.ROLE_ID_NULL}") Long roleId) {
}
