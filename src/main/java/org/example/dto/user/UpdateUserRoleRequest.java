package org.example.dto.user;

import jakarta.validation.constraints.NotNull;
import org.example.common.enums.RoleCode;

public record UpdateUserRoleRequest(@NotNull(message = "{error.ROLE_CODE_NULL}") RoleCode roleCode) {
}
