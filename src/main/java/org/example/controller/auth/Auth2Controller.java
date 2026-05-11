package org.example.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.dto.auth.AuthToken;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.RefreshTokenRequest;
import org.example.dto.auth.SignUpRequest;
import org.example.services.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/auth2")
@RequiredArgsConstructor
@Slf4j
public class Auth2Controller {
    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<AuthToken>> signIn(@RequestBody @Valid LoginRequest payload) {
        var token = authService.login(payload);

        return ResponseEntity.ok()
                .body(ApiResponse.success(token));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody @Valid SignUpRequest payload, HttpServletRequest request) {
        authService.signUp(payload);
        return ResponseEntity.created(URI.create(request.getRequestURI()))
                .body(ApiResponse.success("Sign up success"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthToken>> refresh(@RequestBody @Valid RefreshTokenRequest payload) {
        var token = authService.refresh(payload.refreshToken());
        return ResponseEntity.ok()
                .body(ApiResponse.success(token));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
    ) {
        authService.logout();
        return ResponseEntity.ok()
                .body(ApiResponse.success("Logout success"));
    }
}
