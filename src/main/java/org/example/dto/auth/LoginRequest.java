package org.example.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "error.NOT_BLANK")
        @Size(min = 1, max = 50, message = "error.LENGTH_1_50")
        String username,

        @NotBlank(message = "error.NOT_BLANK")
        @Size(min = 1, max = 255, message = "error.LENGTH_1_255")
        String password
) {
}
