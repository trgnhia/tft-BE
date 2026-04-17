package org.example.services;

import org.example.dto.permission.CreatePermissionRequest;
import org.example.dto.permission.UpdatePermissionRequest;
import org.example.dto.user.PermissionDto;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PermissionService {
    List<PermissionDto> getPermissions(String keyword, Pageable pageable);

    PermissionDto getPermissionDetail(Long id);

    PermissionDto create(CreatePermissionRequest request);

    PermissionDto update(Long id, UpdatePermissionRequest request);

    void delete(Long id);
}
