package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.annotations.Password;
import org.example.common.enums.RoleCode;

public record CreateUserRequest(
        @NotBlank String userName,
        @NotBlank @Email String email,
        @Password String defaultPassword,
        @NotNull Long roleId,
        @NotNull RoleCode roleCode) {
}
