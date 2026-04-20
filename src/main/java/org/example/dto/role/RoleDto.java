package org.example.dto.role;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.dto.user.PermissionDto;

import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RoleDto(Long id, String code, String name, String description, List<PermissionDto> permissions,
                      Boolean deleted, Instant createdAt, Instant updatedAt, Long createdBy, Long updatedBy) {
}
