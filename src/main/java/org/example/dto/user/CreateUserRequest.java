package org.example.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.annotations.Password;

public record CreateUserRequest(
        @NotBlank
        String username,

        @NotBlank
        @Email(message = "{error.EMAIL_INVALID}")
        String email,

        @Password
        String defaultPassword,

        @NotNull
        Long roleId,

        @NotNull
        String roleCode) {
}