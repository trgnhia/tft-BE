package org.example.services;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

@Service
public interface CookieService {
    ResponseCookie createAccessTokenCookie(String accessToken);

    ResponseCookie createRefreshTokenCookie(String refreshToken);

    ResponseCookie cleanAccessTokenCookie();

    ResponseCookie cleanRefreshTokenCookie();
}
