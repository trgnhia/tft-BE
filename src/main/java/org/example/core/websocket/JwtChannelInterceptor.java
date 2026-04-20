package org.example.core.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.security.Principal;

@Slf4j
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
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Principal principal = accessor.getUser();

            if (principal == null) {
                log.warn("STOMP CONNECT rejected: no authenticated principal in websocket session");
                throw new ServerException(ErrorCode.ACCESS_DENIED);
            }

            log.info("STOMP CONNECT accepted for userId={}", principal.getName());
        }
        return message;
    }
}
