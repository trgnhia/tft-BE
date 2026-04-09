package org.example.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    /**
     * Cấu hình message broker cho WebSocket.
     *
     * - "/topic": prefix dùng để server broadcast message cho client
     *
     * @param registry cấu hình broker và routing message
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
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
    }
}