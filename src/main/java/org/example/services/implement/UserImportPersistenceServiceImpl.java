package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.dto.user.UserImportDto;
import org.example.entities.User;
import org.example.repositories.RoleRepository;
import org.example.repositories.UserRepository;
import org.example.services.UserImportPersistenceService;
import org.example.validators.UserBusinessValidator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserImportPersistenceServiceImpl implements UserImportPersistenceService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserBusinessValidator userBusinessValidator;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void persist(UserImportDto dto) {
        userBusinessValidator.validateUserUniqueness(dto.getUsername(), dto.getEmail());

        var role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + dto.getRoleId()));

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setEnabled(Boolean.TRUE.equals(dto.getEnabled()));
        user.setDeleted(false);

        userRepository.save(user);
    }
}
