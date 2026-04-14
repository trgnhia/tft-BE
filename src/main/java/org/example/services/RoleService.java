package org.example.services;

import org.example.dto.role.CreateRoleRequest;
import org.example.dto.role.RoleDto;
import org.example.dto.role.UpdateRolePermissionRequest;
import org.example.dto.role.UpdateRoleRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
    RoleDto createRole(CreateRoleRequest request);

    RoleDto updateRolePermissions(Long id, UpdateRolePermissionRequest request);

    RoleDto deleteRole(Long id);

    Page<RoleDto> getAll(String keyword, Pageable pageable);

    RoleDto getById(Long id);

    RoleDto updateRole(Long id, UpdateRoleRequest updateRoleRequest);
}
