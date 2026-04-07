package org.example.services.implement;

import org.example.security.SecurityUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Bỏ qua nếu không có authentication hoặc chưa xác thực
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        // Chỉ ép kiểu khi principal thực sự là object SecurityUser
        // Điều này giúp tránh lỗi ClassCastException khi principal là chuỗi "anonymousUser"
        if (principal instanceof SecurityUser securityUser) {
            return Optional.of(securityUser.getId());
        }

        // Nếu là String ("anonymousUser") hoặc các kiểu khác, trả về rỗng
        return Optional.empty();
    }
}
