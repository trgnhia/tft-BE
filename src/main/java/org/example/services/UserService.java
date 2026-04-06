package org.example.services;

import org.example.core.api.PageResponse;
import org.example.dto.user.CreateUserRequest;
import org.example.dto.user.UpdateUserRoleRequest;
import org.example.dto.user.UserResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    PageResponse<UserResponse> getAllUser(Pageable pageable);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);
}
