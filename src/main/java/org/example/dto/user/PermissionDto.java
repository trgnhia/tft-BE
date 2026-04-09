package org.example.dto.user;

import lombok.Builder;

@Builder
public record PermissionDto(String permission, String description) {
}
