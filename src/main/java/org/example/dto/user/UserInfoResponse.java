package org.example.dto.user;

import lombok.Builder;

import java.util.List;

/**
 * Lean DTO for session check and basic user info.
 */
@Builder
public record UserInfoResponse(
        String username,
        String email,
        String role,
        List<PermissionDto> permissions
) {
}
