package org.example.services;

import org.example.dto.auth.AuthToken;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.SignUpRequest;
import org.springframework.stereotype.Service;

@Service
public interface AuthService {
    AuthToken login(LoginRequest request);

    void signUp(SignUpRequest request);

    void logout();

    AuthToken refresh(String refreshToken);
}
