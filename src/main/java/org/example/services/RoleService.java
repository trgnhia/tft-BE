package org.example.services;

import org.example.dto.role.CreateRoleRequest;
import org.example.dto.role.RoleDto;
import org.example.dto.role.UpdateRolePermissionRequest;
import org.example.dto.role.UpdateRoleRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    RoleDto createRole(CreateRoleRequest request);

    RoleDto updateRolePermissions(Long id, UpdateRolePermissionRequest request);

    RoleDto deleteRole(Long id);

    List<RoleDto> getAll(String keyword);

    RoleDto getById(Long id);

    RoleDto updateRole(Long id, UpdateRoleRequest updateRoleRequest);
}
