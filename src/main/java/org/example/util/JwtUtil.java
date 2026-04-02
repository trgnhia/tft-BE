package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.configuration.SecurityProperties;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final SecurityProperties securityProperties;
    private SecretKey secretKey;

    private SecretKey getSigningKey() {
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(securityProperties.getJwtSecretKey()));
        }
        return secretKey;
    }

    public String generateAccessToken(UserDetails userDetails, Instant instant) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .expiration(Date.from(instant.plusMillis(securityProperties.getAccessTokenExpirationMs())))
                .issuedAt(Date.from(instant))
                .signWith(getSigningKey())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails, Instant instant) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .expiration(Date.from(instant.plusMillis(securityProperties.getRefreshTokenExpirationMs())))
                .issuedAt(Date.from(instant))
                .signWith(getSigningKey())
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid token attempt: {}", e.getMessage());
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public boolean isIssuedAfterLogout(String token, Instant lastLogoutAt) {
        if (lastLogoutAt == null) return true;
        Date issuedAt = extractAllClaims(token).getIssuedAt();
        return issuedAt.toInstant().isAfter(lastLogoutAt);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
