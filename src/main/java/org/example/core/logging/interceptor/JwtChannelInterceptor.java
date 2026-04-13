package org.example.core.logging.interceptor;

import lombok.RequiredArgsConstructor;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ServerException;
import org.example.security.SecurityUser;
import org.example.util.JwtUtil;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    /**
     *
     * Đây là nơi mọi STOMP frame đi qua
     */
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // Lấy STOMP header accessor
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        // Chỉ xử lý tại thời điểm CONNECT
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            // Lấy header Authorization từ STOMP CONNECT
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServerException(ErrorCode.ACCESS_DENIED);
            }

            String token = authHeader.substring(7);

            // Validate JWT
            if (!jwtUtil.isValidToken(token)) {
                throw new ServerException(ErrorCode.ACCESS_DENIED);
            }

            // Lấy username từ token
            String username = jwtUtil.getUsernameFromToken(token);

            // Load user từ DB (qua UserDetailsService)
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Cast về SecurityUser
            SecurityUser securityUser = (SecurityUser) userDetails;

            // Tạo Authentication object
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            securityUser,
                            null,
                            securityUser.getAuthorities()
                    ) {
                        @Override
                        public String getName() {
                            return String.valueOf(securityUser.getId());
                        }
                    };

            /**
             * tham số : java.security.Principal
             * UsernamePasswordAuthenticationToken implements Authentication
             * Authentication extends Principal
             * Spring dùng principal.getName() để map toi /user/{principalName}/queue/messages
             */
            accessor.setUser(authentication);

            // set Principal cho WebSocket session
            accessor.setUser(authentication);
        }

        return message;
    }
}
