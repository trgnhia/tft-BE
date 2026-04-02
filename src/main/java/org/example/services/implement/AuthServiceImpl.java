package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enums.ErrorCode;
import org.example.common.enums.RoleCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.ServerException;
import org.example.dto.auth.AuthToken;
import org.example.dto.auth.LoginRequest;
import org.example.dto.auth.SignUpRequest;
import org.example.entities.Role;
import org.example.entities.User;
import org.example.repositories.RoleRepository;
import org.example.repositories.UserRepository;
import org.example.services.AuthService;
import org.example.util.JwtUtil;
import org.example.util.SecurityUtil;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Override
    public AuthToken login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Nếu code chạy xuống được đây nghĩa là thông tin hợp lệ. Lấy thông tin user ra.
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return buildToken(userDetails);
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
        if (userRepository.existsByUsername(request.userName())) {
            throw new ConflictException("Username");
        }
        User user = buildUser(request);
        userRepository.save(user);
    }

    private @NonNull User buildUser(SignUpRequest request) {
        Role defaultRole = roleRepository.findByCode(RoleCode.USER)
                .orElseThrow(() -> new ServerException(ErrorCode.SERVICE_UNAVAILABLE));
        User user = new User();
        user.setUsername(request.userName());
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

        user.setLastLogoutAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    public AuthToken refresh(String refreshToken) {
        jwtUtil.validateToken(refreshToken);
        String userName = jwtUtil.getUsernameFromToken(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(userName);
        return buildToken(userDetails);
    }
}
