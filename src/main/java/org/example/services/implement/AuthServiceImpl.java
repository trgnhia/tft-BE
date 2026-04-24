package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ServerException;
import org.example.dto.auth.AuthToken;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.SignUpRequest;
import org.example.entities.Role;
import org.example.entities.User;
import org.example.repositories.RoleRepository;
import org.example.repositories.UserRepository;
import org.example.security.SecurityUser;
import org.example.services.AuthService;
import org.example.util.JwtUtil;
import org.example.util.SecurityUtil;
import org.example.validators.UserBusinessValidator;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final UserBusinessValidator userBusinessValidator;
    private final JwtUtil jwtUtil;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthToken login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
            
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return buildToken(userDetails);
        } catch (AuthenticationException e) {
            throw new ServerException(ErrorCode.PASSWORD_INCORRECT);
        }
    }

    private AuthToken buildToken(UserDetails userDetails) {
        Instant now = Instant.now();
        return AuthToken.builder()
                .accessToken(jwtUtil.generateAccessToken(userDetails, now))
                .refreshToken(jwtUtil.generateRefreshToken(userDetails, now))
                .build();
    }

    @Override
    @Transactional
    public void signUp(SignUpRequest request) {
        userBusinessValidator.validateUserUniqueness(request.username(), request.email());
        User user = buildUser(request);
        userRepository.save(user);
    }

    private @NonNull User buildUser(SignUpRequest request) {
        Role defaultRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new ServerException(ErrorCode.SERVICE_UNAVAILABLE));
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(defaultRole);
        return user;
    }

    @Override
    @Transactional
    public void logout() {
        User user = userRepository.findById(securityUtil.getCurrentUserIdOrThrow())
                .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));

        user.logout();
        userRepository.save(user);
    }

    @Override
    public AuthToken refresh(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) {
            throw new ServerException(ErrorCode.MISSING_TOKEN);
        }

        if (!jwtUtil.isValidToken(refreshToken)) {
            throw new ServerException(ErrorCode.SESSION_EXPIRED);
        }

        String userName = jwtUtil.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        if (userDetails instanceof SecurityUser securityUser && !jwtUtil.isIssuedAfterLogout(refreshToken, securityUser.getLastLogoutAt())) {
            throw new ServerException(ErrorCode.SESSION_REVOKED);
        }

        return buildToken(userDetails);
    }
}
