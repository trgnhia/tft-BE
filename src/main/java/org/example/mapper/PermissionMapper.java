package org.example.mapper;

import org.example.dto.user.PermissionDto;
import org.example.entities.Permission;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toDto(Permission permission);

    Permission toEntity(PermissionDto permissionDto);
}
