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
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final SecurityProperties securityProperties;
    private SecretKey secretKey;

    private SecretKey getSigningKey() {
        // Đổi sang BASE64
        byte[] keyBytes = Decoders.BASE64.decode(securityProperties.getJwtSecretKey());
        return Keys.hmacShaKeyFor(keyBytes);
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

    public boolean isValidToken(String token) {
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

        Instant issueInstant = issuedAt.toInstant().truncatedTo(ChronoUnit.SECONDS);
        Instant logoutInstant = lastLogoutAt.truncatedTo(ChronoUnit.SECONDS);

        return issueInstant.isAfter(logoutInstant);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
