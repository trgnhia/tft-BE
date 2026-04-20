package org.example.services.implement;

import org.example.services.AssetUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@Service
public class AssetUrlServiceImpl implements AssetUrlService {

    private final String publicBaseUrl;

    public AssetUrlServiceImpl(@Value("${app.public-base-url:}") String publicBaseUrl) {
        this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
    }

    @Override
    public String toPublicUrl(String pathOrUrl) {
        if (!StringUtils.hasText(pathOrUrl)) {
            return pathOrUrl;
        }

        String normalized = pathOrUrl.trim();
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) {
            return normalized;
        }

        String path = normalized.startsWith("/") ? normalized : "/" + normalized;
        String baseUrl = resolveBaseUrl();
        if (!StringUtils.hasText(baseUrl)) {
            return path;
        }
        return baseUrl + path;
    }

    private String resolveBaseUrl() {
        try {
            return normalizeBaseUrl(ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString());
        } catch (IllegalStateException exception) {
            return publicBaseUrl;
        }
    }

    private String normalizeBaseUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
