package org.example.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.configuration.SecurityProperties;
import org.example.util.JwtUtil;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final HandlerExceptionResolver exceptionResolver;
    private final SecurityProperties securityProperties;

    public JwtAuthFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService,
                         @Qualifier("handlerExceptionResolver") HandlerExceptionResolver exceptionResolver,
                         SecurityProperties securityProperties) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.exceptionResolver = exceptionResolver;
        this.securityProperties = securityProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = extractTokenFromCookie(request);
        try {
            if (isValidToken(token)) {
                authenticateUser(request, token);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            SecurityContextHolder.clearContext();
            exceptionResolver.resolveException(request, response, null, ex);
        }
    }

    private void authenticateUser(HttpServletRequest request, String token) {
        String username = jwtUtil.getUsernameFromToken(token);
        SecurityUser user = (SecurityUser) userDetailsService.loadUserByUsername(username);

        if (user.isEnabled() && jwtUtil.isIssuedAfterLogout(token, user.getLastLogoutAt())) {
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
    }

    private boolean isValidToken(String token) {
        return token != null && jwtUtil.isValidToken(token);
    }

    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals(securityProperties.getAccessTokenCookie()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }
}
