package org.example.mapper;

import org.example.dto.role.CreateRoleRequest;
import org.example.dto.role.RoleDto;
import org.example.dto.role.UpdateRoleRequest;
import org.example.entities.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toEntity(CreateRoleRequest request);

    Role toEntity(UpdateRoleRequest request);

    RoleDto toDto(Role role);
}
