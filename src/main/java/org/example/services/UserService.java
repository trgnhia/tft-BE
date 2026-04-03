package org.example.services;

import org.example.dto.user.CreateUserRequest;
import org.example.dto.user.UserResponse;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
    UserResponse createUser(CreateUserRequest request);
}
