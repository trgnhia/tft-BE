package org.example.configuration;

import lombok.RequiredArgsConstructor;
import org.example.core.logging.interceptor.JwtChannelInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;
    /**
     * Cấu hình broker và routing cho WebSocket.
     * /app   : client gửi message lên server để @MessageMapping xử lý
     * /topic : server broadcast cho nhiều client
     * /queue : server gửi message theo dạng point-to-point / user-specific
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.setApplicationDestinationPrefixes("/app");
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setUserDestinationPrefix("/user");
    }

    /**
     * Đăng ký endpoint để client kết nối WebSocket.
     *
     * - "/ws": URL để FE connect (ws://localhost:8080/ws)
     * - Cho phép tất cả domain để tránh lỗi CORS khi FE khác domain
     * @param registry cấu hình endpoint WebSocket
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
        // .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(jwtChannelInterceptor);
    }
}