package org.example.services.implement;

import org.example.services.AssetUrlService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;

@Service
public class AssetUrlServiceImpl implements AssetUrlService {

    private static final String MANAGED_UPLOAD_BASE_PATH = "/uploads";

    private final String contextPath;
    private final String publicBaseUrl;

    public AssetUrlServiceImpl(
            @Value("${server.servlet.context-path:}") String contextPath,
            @Value("${app.public-base-url:}") String publicBaseUrl
    ) {
        this.contextPath = normalizeContextPath(contextPath);
        this.publicBaseUrl = normalizeBaseUrl(publicBaseUrl);
    }

    @Override
    public String toPublicUrl(String pathOrUrl) {
        if (!StringUtils.hasText(pathOrUrl)) {
            return pathOrUrl;
        }

        String normalizedInput = pathOrUrl.trim();
        if (normalizedInput.startsWith("http://") || normalizedInput.startsWith("https://")) {
            try {
                URI uri = URI.create(normalizedInput);
                String normalizedManagedPath = normalizeManagedUploadPathFromAbsolute(uri.getPath());
                return normalizedManagedPath != null ? normalizedManagedPath : normalizedInput;
            } catch (IllegalArgumentException exception) {
                return normalizedInput;
            }
        }

        String normalizedManagedPath = normalizeManagedUploadPath(normalizedInput);
        if (normalizedManagedPath != null) {
            return normalizedManagedPath;
        }
        return normalizedInput;
    }

    private String normalizeManagedUploadPath(String rawPath) {
        if (!StringUtils.hasText(rawPath)) {
            return null;
        }

        String path = rawPath.trim().replace("\\", "/");
        if (path.startsWith("uploads/")) {
            return "/" + path;
        }
        if (path.startsWith(MANAGED_UPLOAD_BASE_PATH + "/")) {
            return path;
        }

        String normalizedContextPrefix = contextPath + MANAGED_UPLOAD_BASE_PATH + "/";
        if (StringUtils.hasText(contextPath) && path.startsWith(normalizedContextPrefix)) {
            return path.substring(contextPath.length());
        }

        String fallbackContextPath = extractContextPathFromPublicBaseUrl();
        String fallbackContextPrefix = fallbackContextPath + MANAGED_UPLOAD_BASE_PATH + "/";
        if (StringUtils.hasText(fallbackContextPath) && path.startsWith(fallbackContextPrefix)) {
            return path.substring(fallbackContextPath.length());
        }

        return null;
    }

    private String normalizeManagedUploadPathFromAbsolute(String absolutePath) {
        if (!StringUtils.hasText(absolutePath)) {
            return null;
        }

        String normalized = absolutePath.trim().replace("\\", "/");
        if (normalized.startsWith("/api/")) {
            return normalizeManagedUploadPath(normalized);
        }
        if (StringUtils.hasText(contextPath) && normalized.startsWith(contextPath + "/")) {
            return normalizeManagedUploadPath(normalized);
        }
        return null;
    }

    private String extractContextPathFromPublicBaseUrl() {
        if (!StringUtils.hasText(publicBaseUrl)) {
            return "";
        }
        try {
            URI baseUri = URI.create(publicBaseUrl);
            String contextPath = baseUri.getPath();
            if (!StringUtils.hasText(contextPath) || "/".equals(contextPath)) {
                return "";
            }
            return normalizeContextPath(contextPath);
        } catch (IllegalArgumentException exception) {
            return "";
        }
    }

    private String normalizeBaseUrl(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        String normalized = value.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }

    private String normalizeContextPath(String value) {
        if (!StringUtils.hasText(value) || "/".equals(value.trim())) {
            return "";
        }
        String normalized = value.trim();
        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
