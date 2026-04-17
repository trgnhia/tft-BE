package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.permission.CreatePermissionRequest;
import org.example.dto.permission.UpdatePermissionRequest;
import org.example.dto.user.PermissionDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface PermissionService {
    PageResponse<PermissionDto> getPermissions(String keyword, Pageable pageable);

    PermissionDto getPermissionDetail(Long id);

    PermissionDto create(CreatePermissionRequest request);

    PermissionDto update(Long id, UpdatePermissionRequest request);

    void delete(Long id);
}
