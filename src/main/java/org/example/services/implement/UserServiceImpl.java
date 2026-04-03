package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.enums.RoleCode;
import org.example.common.exception.DataException;
import org.example.dto.user.CreateUserRequest;
import org.example.dto.user.UserResponse;
import org.example.entities.Role;
import org.example.entities.User;
import org.example.repositories.RoleRepository;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.example.validators.UserBusinessValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserBusinessValidator userBusinessValidator;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        userBusinessValidator.validateUserUniqueness(request.userName(), request.email());
        User user = new User();
        user.setUsername(request.userName());
        user.setEmail(request.email());
        Role role = getOrThrow(request.roleCode());
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(request.defaultPassword()));
        User saved = userRepository.save(user);

        return mapToUserResponse(saved);
    }

    private UserResponse mapToUserResponse(User saved) {
        return UserResponse.builder()
                .userName(saved.getUsername())
                .email(saved.getEmail())
                .roleId(saved.getRole().getId())
                .roleName(saved.getRole().getName())
                .build();
    }

    public Role getOrThrow(RoleCode roleCode) {
        return roleRepository.findByCode(roleCode)
                .orElseThrow(() -> new DataException(ErrorCode.NOT_FOUND, "Role"));
    }
}
