package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.annotations.Password;
import org.example.common.enums.RoleCode;

public record CreateUserRequest(
        @NotBlank(message = "{error.USERNAME_BLANK}")
        String userName,

        @NotBlank(message = "{error.EMAIL_BLANK}")
        @Email(message = "{error.EMAIL_INVALID}")
        String email,

        @Password
        String defaultPassword,

        @NotNull(message = "{error.ROLE_ID_NULL}")
        Long roleId,

        @NotNull(message = "{error.ROLE_CODE_NULL}")
        RoleCode roleCode) {
}