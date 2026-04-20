package org.example.services.implement;

import org.example.services.AssetUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

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

        String normalizedPath = normalizePathAgainstBase(path, baseUrl);
        return baseUrl + normalizedPath;
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

    private String normalizePathAgainstBase(String path, String baseUrl) {
        try {
            URI baseUri = URI.create(baseUrl);
            String contextPath = baseUri.getPath();
            if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
                return path;
            }

            String normalizedContextPath = contextPath.endsWith("/")
                    ? contextPath.substring(0, contextPath.length() - 1)
                    : contextPath;
            String contextPrefix = normalizedContextPath + "/";

            if (path.startsWith(contextPrefix)) {
                return path.substring(normalizedContextPath.length());
            }
            return path;
        } catch (IllegalArgumentException exception) {
            return path;
        }
    }
}
