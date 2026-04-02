package org.example.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "server.security")
@Getter
public class SecurityProperties {
    private String jwtSecretKey;
    private boolean secure;
    private Long accessTokenExpirationMs;
    private Long refreshTokenExpirationMs;
    private String refreshPath;
    private String accessTokenCookie;
    private String refreshTokenCookie;
}
