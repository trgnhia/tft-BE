package org.example.dto.user;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountStatusRequest(@NotNull Boolean enabled) {
}
