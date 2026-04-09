package org.example.dto.user;

import lombok.Builder;

@Builder
public record UserResponse(String userName,
                           String email,
                           Long roleId,
                           String roleName) {
}
