package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.configuration.SecurityProperties;
import org.example.services.CookieService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class CookieServiceImpl implements CookieService {
    private final SecurityProperties securityProperties;
    @Value("${server.servlet.context-path}")
    private String apiPrefix;

    @Override
    public ResponseCookie createAccessTokenCookie(String accessToken) {
        return buildCookieToken(securityProperties.getAccessTokenCookie(),
                accessToken,
                apiPrefix,
                securityProperties.getAccessTokenExpirationMs(),
                "Strict");
    }

    @Override
    public ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return buildCookieToken(securityProperties.getRefreshTokenCookie(),
                refreshToken,
                securityProperties.getRefreshPath(),
                securityProperties.getRefreshTokenExpirationMs(),
                "Strict");
    }

    @Override
    public ResponseCookie cleanAccessTokenCookie() {
        return buildCleanCookie(securityProperties.getAccessTokenCookie(), apiPrefix);
    }

    @Override
    public ResponseCookie cleanRefreshTokenCookie() {
        return buildCleanCookie(securityProperties.getRefreshTokenCookie(), securityProperties.getRefreshPath());
    }

    private ResponseCookie buildCookieToken(String cookieName, String value, String path, long maxAgeMs, String sameSite) {
        return ResponseCookie.from(cookieName, value)
                .httpOnly(true)
                .secure(securityProperties.isSecure())
                .path(path)
                .maxAge(Duration.ofMillis(maxAgeMs))
                .sameSite(sameSite)
                .build();
    }

    private ResponseCookie buildCleanCookie(String name, String path) {
        return ResponseCookie.from(name)
                .httpOnly(true)
                .secure(securityProperties.isSecure())
                .path(path)
                .maxAge(0)
                .build();
    }
}
