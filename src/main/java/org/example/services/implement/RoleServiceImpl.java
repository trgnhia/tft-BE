package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.enums.RoleCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ServerException;
import org.example.dto.role.CreateRoleRequest;
import org.example.dto.role.RoleDto;
import org.example.dto.role.UpdateRolePermissionRequest;
import org.example.dto.role.UpdateRoleRequest;
import org.example.entities.Permission;
import org.example.entities.Role;
import org.example.mapper.RoleMapper;
import org.example.repositories.PermissionRepository;
import org.example.repositories.RoleRepository;
import org.example.services.RoleService;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public RoleDto createRole(CreateRoleRequest request) {
        String normalizedCode = request.code().toUpperCase();
        validateUniqueness(normalizedCode, request.name());

        Role newRole = roleMapper.toEntity(request);
        newRole.setCode(normalizedCode);

        Role saved = roleRepository.save(newRole);
        return roleMapper.toDto(saved);
    }

    @Override
    public RoleDto updateRolePermissions(Long id, UpdateRolePermissionRequest request) {
        Role role = getRoleWithPermissionOrThrow(id);

        if (role.getCode().equals(RoleCode.ADMIN.toString())) throw new ServerException(ErrorCode.UPDATE_ADMIN_ROLE);

        List<Permission> foundPermissions = new ArrayList<>(permissionRepository.findAllById(request.permissionIds()));
        validatePermissions(foundPermissions, request.permissionIds());

        role.setPermissions(foundPermissions);
        Role updatedRole = roleRepository.save(role);

        return roleMapper.toDto(updatedRole);
    }

    @Override
    public void deleteRole(Long id) {
        Role role = getRoleWithPermissionOrThrow(id);
        if (Arrays.stream(RoleCode.values()).anyMatch(roleCode -> roleCode.toString().equals(role.getCode()))) {
            throw new ServerException(ErrorCode.DELETE_BASIC_ROLE);
        }
        roleRepository.delete(role);
    }

    @Override
    public List<RoleDto> getAll(String keyword) {
        if (keyword == null || keyword.trim().isBlank()) {
            return roleRepository.findAll()
                    .stream()
                    .map(roleMapper::toDto)
                    .toList();
        }
        return roleRepository.findAllWithKeyword(keyword.toUpperCase())
                .stream()
                .map(roleMapper::toDto)
                .toList();
    }

    @Override
    public RoleDto getById(Long id) {
        return roleMapper.toDto(getRoleWithPermissionOrThrow(id));
    }

    @Override
    public RoleDto updateRole(Long id, UpdateRoleRequest request) {
        String normalizedCode = request.code().toUpperCase();
        validateUniqueness(normalizedCode, request.name());
        Role roleToUpdate = getRoleOrThrow(id);

        roleToUpdate.setName(request.name());
        roleToUpdate.setCode(normalizedCode);
        roleToUpdate.setDescription(request.description());

        Role saved = roleRepository.save(roleToUpdate);
        return roleMapper.toDto(saved);
    }

    private void validatePermissions(List<Permission> foundPermissions, Set<Long> requestedIds) {
        if (foundPermissions.size() != requestedIds.size()) {
            Set<Long> foundIds = foundPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = new HashSet<>(requestedIds);

            missingIds.removeAll(foundIds);
            throw new DataException(ErrorCode.NOT_FOUND, missingIds.toArray());
        }
    }

    private Role getRoleWithPermissionOrThrow(Long id) {
        return roleRepository.findByIdWithPermissions(id)
                .orElseThrow(() -> new DataException(ErrorCode.NOT_FOUND, "Role Id: " + id));
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new DataException(ErrorCode.NOT_FOUND, "Role Id: " + id));
    }

    private void validateUniqueness(String roleCode, String roleName) {
        if (roleRepository.existsByCode(roleCode.toUpperCase())) {
            throw new ConflictException(roleCode);
        }
        if (roleRepository.existsByName(roleName)) {
            throw new ConflictException(roleName);
        }
    }
}
