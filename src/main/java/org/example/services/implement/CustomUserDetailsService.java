package org.example.services.implement;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ServerException;
import org.example.entities.User;
import org.example.repositories.UserRepository;
import org.example.security.SecurityUser;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ServerException(ErrorCode.PASSWORD_INCORRECT));
        return new SecurityUser(user);
    }
}
