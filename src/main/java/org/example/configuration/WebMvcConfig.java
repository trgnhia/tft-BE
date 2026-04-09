package org.example.configuration;

import lombok.RequiredArgsConstructor;
import org.example.core.logging.interceptor.CmsLogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final CmsLogInterceptor cmsLogInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Đăng ký Interceptor vào hệ thống MVC
        registry.addInterceptor(cmsLogInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/signin",
                        "/auth/signup",
                        "/v3/api-docs/**",
                        "/swagger-ui/**"
                );
    }
}