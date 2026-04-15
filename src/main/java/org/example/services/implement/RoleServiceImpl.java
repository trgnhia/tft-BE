package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.dto.role.CreateRoleRequest;
import org.example.dto.role.RoleDto;
import org.example.dto.role.UpdateRolePermissionRequest;
import org.example.dto.role.UpdateRoleRequest;
import org.example.dto.user.PermissionDto;
import org.example.entities.Permission;
import org.example.entities.Role;
import org.example.mapper.RoleMapper;
import org.example.repositories.PermissionRepository;
import org.example.repositories.RoleRepository;
import org.example.services.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
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

        if (request.permissions() != null && !request.permissions().isEmpty()) {
            Set<Permission> attachedPermissions = request.permissions().stream()
                    .map(dto -> permissionRepository.getReferenceById(dto.id()))
                    .collect(Collectors.toSet());
            newRole.setPermissions(attachedPermissions);
        }

        Role saved = roleRepository.save(newRole);
        return roleMapper.toDto(saved);
    }

    @Override
    public RoleDto updateRolePermissions(Long id, UpdateRolePermissionRequest request) {
        Role role = getRoleOrThrow(id);

        Set<Long> requestedIds = request.permissions().stream()
                .map(PermissionDto::id)
                .collect(Collectors.toSet());

        Set<Permission> foundPermissions = new HashSet<>(permissionRepository.findAllById(requestedIds));
        validatePermissions(foundPermissions, requestedIds);

        role.setPermissions(foundPermissions);
        Role updatedRole = roleRepository.save(role);

        return roleMapper.toDto(updatedRole);
    }

    @Override
    public RoleDto deleteRole(Long id) {
        Role role = getRoleOrThrow(id);
        role.setDeleted(true);
        Role saved = roleRepository.save(role);
        return roleMapper.toDto(saved);
    }

    @Override
    public Page<RoleDto> getAll(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isBlank()) {
            return roleRepository.findAll(pageable)
                    .map(roleMapper::toDto);
        }
        return roleRepository.findAllWithKeyword(keyword.toUpperCase(), pageable)
                .map(roleMapper::toDto);
    }

    @Override
    public RoleDto getById(Long id) {
        return roleMapper.toDto(getRoleOrThrow(id));
    }

    @Override
    public RoleDto updateRole(Long id, UpdateRoleRequest request) {
        String normalizedCode = request.code().toUpperCase();
        validateUniqueness(normalizedCode, request.name());

        Role newRole = roleMapper.toEntity(request);
        newRole.setCode(normalizedCode);

        Role saved = roleRepository.save(newRole);
        return roleMapper.toDto(saved);
    }

    private void validatePermissions(Set<Permission> foundPermissions, Set<Long> requestedIds) {
        if (foundPermissions.size() != requestedIds.size()) {
            Set<Long> foundIds = foundPermissions.stream()
                    .map(Permission::getId)
                    .collect(Collectors.toSet());

            Set<Long> missingIds = new HashSet<>(requestedIds);

            missingIds.removeAll(foundIds);
            throw new DataException(ErrorCode.NOT_FOUND, missingIds.toArray());
        }
    }

    private Role getRoleOrThrow(Long id) {
        return roleRepository.findByIdWithPermissions(id)
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
