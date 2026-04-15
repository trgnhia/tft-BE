package org.example.services;

import jakarta.validation.Valid;
import org.example.core.api.PageResponse;
import org.example.dto.user.*;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    PageResponse<UserResponse> getAllUser(UserFilter userFilter, Pageable pageable);

    UserResponse createUser(CreateUserRequest request);

    UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request);

    UserDetailedResponse getDetailedById(Long userId);

    UserDetailedResponse updateUserProfile(Long userId, @Valid UpdateUserProfileRequest request);

    UserDetailedResponse deleteUserById(Long userId);

    UserDetailedResponse updateStatus(Long userId, UpdateAccountStatusRequest request);

    UserInfoResponse getMyInfo(Long userId);
}
