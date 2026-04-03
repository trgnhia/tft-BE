package org.example.validators;

import lombok.RequiredArgsConstructor;
import org.example.common.exception.ConflictException;
import org.example.repositories.UserRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBusinessValidator {
    private final UserRepository userRepository;

    public void validateUserUniqueness(String username, String email) {
        if (userRepository.existsByUsername(username)) {
            throw new ConflictException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists");
        }
    }
}
