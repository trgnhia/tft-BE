package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.DataException;
import org.example.core.api.PageResponse;
import org.example.dto.user.*;
import org.example.entities.Permission;
import org.example.entities.Role;
import org.example.entities.User;
import org.example.repositories.RoleRepository;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.example.validators.UserBusinessValidator;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserBusinessValidator userBusinessValidator;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;


    @Override
    public PageResponse<UserResponse> getAllUser(UserFilter userFilter, Pageable pageable) {
        var userPaged = userRepository.findAllByFilter(userFilter.userName(), userFilter.email(), userFilter.roleId(), userFilter.enabled(), pageable)
                .map(this::mapToUserResponse);
        return PageResponse.from(userPaged);
    }

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userBusinessValidator.validateUserUniqueness(request.userName(), request.email());
        User user = new User();
        user.setUsername(request.userName());
        user.setEmail(request.email());
        Role role = getOrThrowRole(request.roleId());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(request.defaultPassword()));
        User saved = userRepository.save(user);

        return mapToUserResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateUserRole(Long userId, UpdateUserRoleRequest request) {
        User user = getOrThrowUser(userId);
        Role newRole = getOrThrowRole(request.roleId());
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return mapToUserResponse(saved);
    }

    @Override
    public UserDetailedResponse getDetailedById(Long userId) {
        User user = getOrThrowUser(userId);
        return mapToUserDetailed(user);
    }

    @Override
    @Transactional
    public UserDetailedResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        userBusinessValidator.validateUserUniqueness(request.userName(), request.email());
        User user = getOrThrowUser(userId);
        Role newRole = getOrThrowRole(request.roleId());
        user.setRole(newRole);
        user.setUsername(request.userName());
        user.setEmail(request.email());
        User saved = userRepository.save(user);
        return mapToUserDetailed(saved);
    }

    @Override
    @Transactional
    public UserDetailedResponse deleteUserById(Long userId) {
        User user = getOrThrowUser(userId);
        user.setDeleted(true);
        var deleted = userRepository.save(user);
        return mapToUserDetailed(deleted);
    }

    @Override
    public UserDetailedResponse updateStatus(Long userId, UpdateAccountStatusRequest request) {
        User user = getOrThrowUser(userId);
        user.setEnabled(request.enabled());
        User saved = userRepository.save(user);
        return mapToUserDetailed(saved);
    }

    @Override
    public UserInfoResponse getMyInfo(Long userId) {
        User user = getOrThrowUser(userId);
        return mapToUserInfo(user);
    }

    private UserInfoResponse mapToUserInfo(User user) {
        return UserInfoResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getCode())
                .permissions(resolvePermissions(user.getRole().getPermissions()))
                .build();
    }

    private UserDetailedResponse mapToUserDetailed(User user) {
        return UserDetailedResponse.builder()
                .userName(user.getUsername())
                .email(user.getEmail())
                .enabled(user.isEnabled())
                .deleted(user.isDeleted())
                .createdDate(user.getCreatedAt())
                .lastLogout(user.getLastLogoutAt())
                .role(user.getRole().getCode())
                .roleDescription(user.getRole().getDescription())
                .permissions(resolvePermissions(user.getRole().getPermissions()))
                .build();
    }

    private List<PermissionDto> resolvePermissions(List<Permission> permissions) {
        return permissions.stream()
                .map(this::toPermissionDto)
                .toList();
    }

    private PermissionDto toPermissionDto(Permission permission) {
        return PermissionDto.builder()
                .code(permission.getCode())
                .description(permission.getDescription())
                .build();
    }

    private UserResponse mapToUserResponse(User saved) {
        return UserResponse.builder()
                .userName(saved.getUsername())
                .email(saved.getEmail())
                .roleId(saved.getRole().getId())
                .roleName(saved.getRole().getName())
                .build();
    }

    private Role getOrThrowRole(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new DataException(ErrorCode.NOT_FOUND, "Role"));
    }

    private User getOrThrowUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new DataException(ErrorCode.NOT_FOUND, "User"));
    }
}
