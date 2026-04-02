package org.example.dto.auth;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record LoginRequest(
        @Min(value = 1, message = "error.MIN_LENGTH") @Max(value = 50, message = "error.MAX_LENGTH") String username,
        @Min(value = 1, message = "error.MIN_LENGTH") @Max(value = 255, message = "error.MAX_LENGTH") String password) {
}
