package org.example.services;


import org.example.common.enums.ErrorCode;
import org.example.common.exception.ServerException;
import org.example.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class BaseService {
    protected Optional<SecurityUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof SecurityUser u) {
            return Optional.of(u);
        }
        return Optional.empty();
    }

    protected String getCurrentUserNameOrThrow() {
        return getCurrentUser()
                .map(SecurityUser::getUsername)
                .orElseThrow(() -> new ServerException(ErrorCode.ACCESS_DENIED));
    }

    protected Long getCurrentUserIdOrThrow() {
        return getCurrentUser()
                .map(SecurityUser::getId)
                .orElseThrow(() -> new ServerException(ErrorCode.ACCESS_DENIED));
    }
}
