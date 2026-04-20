package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.enums.PERMISSION;
import org.example.common.enums.RESOURCE;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.dto.permission.CreatePermissionRequest;
import org.example.dto.permission.UpdatePermissionRequest;
import org.example.dto.user.PermissionDto;
import org.example.entities.Permission;
import org.example.mapper.PermissionMapper;
import org.example.repositories.PermissionRepository;
import org.example.services.PermissionService;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public List<PermissionDto> getPermissions(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return permissionRepository.findAll()
                    .stream()
                    .map(permissionMapper::toDto)
                    .toList();
        }
        return permissionRepository.findWithKeyword(keyword)
                .stream()
                .map(permissionMapper::toDto)
                .toList();
    }

    @Override
    public PermissionDto getPermissionDetail(Long id) {
        Permission permission = getOrThrowPermission(id);
        return permissionMapper.toDto(permission);
    }

    @Override
    public PermissionDto create(CreatePermissionRequest request) {
        RESOURCE resource = RESOURCE.valueOf(request.name().toUpperCase());
        PERMISSION permit = PERMISSION.valueOf(request.permission().toUpperCase());
        String normalizedCode = String.format("%s_%s", resource, permit);

        Permission permission = new Permission();
        permission.setCode(normalizedCode);
        permission.setName(request.name());
        permission.setDescription(request.description());

        validatePermission(permission);
        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toDto(saved);
    }

    @Override
    public PermissionDto update(Long id, UpdatePermissionRequest request) {
        Permission permission = getOrThrowPermission(id);
        permission.setName(request.name());
        permission.setDescription(request.description());
        Permission saved = permissionRepository.save(permission);
        return permissionMapper.toDto(saved);
    }

    private @NonNull Permission getOrThrowPermission(Long id) {
        return permissionRepository.findById(id)
                .orElseThrow(() -> new DataException(ErrorCode.NOT_FOUND, "Permission Id: " + id));
    }

    @Override
    public void delete(Long id) {
        permissionRepository.deleteById(id);
    }

    private void validatePermission(Permission permission) {
        if (permissionRepository.existsByCode(permission.getCode())) {
            throw new ConflictException(permission.getCode());
        }
    }
}
