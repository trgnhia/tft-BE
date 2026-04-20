package org.example.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

import java.time.Instant;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record PermissionDto(Long id, String code, String name, String description, Instant createdAt,
                            Instant updatedAt) {
}
