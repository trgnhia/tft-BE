package org.example.dto.user;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record UserDetailedResponse(String userName, String email, Boolean enabled, Boolean deleted, Instant lastLogout,
                                   Instant createdDate, String role, String roleDescription,
                                   List<PermissionDto> permissions) {
}
