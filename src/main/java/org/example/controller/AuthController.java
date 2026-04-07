package org.example.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.core.api.ApiResponse;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.SignUpRequest;
import org.example.services.AuthService;
import org.example.services.CookieService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final CookieService cookieService;

    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<String>> signIn(@RequestBody @Valid LoginRequest payload, HttpServletRequest request) {
        var token = authService.login(payload);

        var accessTokenCookie = cookieService.createAccessTokenCookie(token.accessToken());
        var refreshTokenCookie = cookieService.createRefreshTokenCookie(token.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success("Login success"));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<String>> signUp(@RequestBody @Valid SignUpRequest payload, HttpServletRequest request) {
        authService.signUp(payload);
        return ResponseEntity.created(URI.create(request.getRequestURI()))
                .body(ApiResponse.success("Sign up success"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<String>> refresh(@CookieValue(name = "refresh-token", required = false) String refreshToken, HttpServletRequest request) {
        var token = authService.refresh(refreshToken);
        var accessTokenCookie = cookieService.createAccessTokenCookie(token.accessToken());
        var refreshTokenCookie = cookieService.createRefreshTokenCookie(token.refreshToken());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(ApiResponse.success("Refresh success"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(
    ) {
        authService.logout();
        var cleanAccessTokenCookie = cookieService.cleanAccessTokenCookie();
        var cleanRefreshTokenCookie = cookieService.cleanRefreshTokenCookie();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanAccessTokenCookie.toString())
                .header(HttpHeaders.SET_COOKIE, cleanRefreshTokenCookie.toString())
                .body(ApiResponse.success("Logout success"));
    }
}