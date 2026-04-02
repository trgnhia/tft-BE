package org.example.dto.auth;

import lombok.Builder;

@Builder
public record AuthToken(String accessToken, String refreshToken) {
}
