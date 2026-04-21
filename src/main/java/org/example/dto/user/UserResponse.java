package org.example.dto.user;

import lombok.Builder;

import java.time.Instant;

@Builder
public record UserResponse(Long id, String username,
                           String email,
                           String roleName,
                           Boolean enabled,
                           Instant createdAt, Instant updatedAt, Long updatedBy) {
}
