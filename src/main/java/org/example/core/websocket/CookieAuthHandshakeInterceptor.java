package org.example.core.websocket;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.security.SecurityUser;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieAuthHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Value("${security.access-token-cookie:access-token}")
    private String accessTokenCookieName;

    public static final String WS_AUTH_PRINCIPAL = "WS_AUTH_PRINCIPAL";

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) {

        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            log.warn("WebSocket handshake failed: request is not ServletServerHttpRequest");
            return false;
        }

        Cookie[] cookies = servletRequest.getServletRequest().getCookies();
        if (cookies == null || cookies.length == 0) {
            log.warn("WebSocket handshake failed: no cookies found");
            return false;
        }

        String token = extractAccessToken(cookies);
        if (token == null || token.isBlank()) {
            log.warn("WebSocket handshake failed: access token cookie '{}' not found", accessTokenCookieName);
            return false;
        }

        if (!jwtUtil.isValidToken(token)) {
            log.warn("WebSocket handshake failed: invalid token");
            return false;
        }

        String username = jwtUtil.getUsernameFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        SecurityUser securityUser = (SecurityUser) userDetails;

        Principal principal = new UsernamePasswordAuthenticationToken(
                securityUser,
                null,
                securityUser.getAuthorities()
        ) {
            @Override
            public String getName() {
                return String.valueOf(securityUser.getId());
            }
        };

        attributes.put(WS_AUTH_PRINCIPAL, principal);

        log.info("WebSocket handshake authenticated successfully for userId={}", principal.getName());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // no-op
    }

    private String extractAccessToken(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            if (accessTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}