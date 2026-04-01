package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Slf4j
@Component
public class JwtUtil {
    @Value(value = "${spring.security.jwt.secret-key}")
    private String jwtSecret;
    @Value(value = "${spring.security.jwt.expiration-ms}")
    private long expirationMs;
    private SecretKey secretKey;

    private SecretKey getSigningKey() {
        if (secretKey == null) {
            secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(jwtSecret));
        }
        return secretKey;
    }

    public String generateToken(String username) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(username)
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .issuedAt(Date.from(now))
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
