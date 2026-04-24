package org.example.services.implement;

import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.DataException;
import org.example.dto.upload.FileUploadResponse;
import org.example.services.FileStorageService;
import org.example.util.MessageUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Locale;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    private static final long DEFAULT_MAX_IMAGE_SIZE_BYTES = 2L * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    private static final List<String> ALLOWED_EXTENSIONS = List.of(".jpg", ".jpeg", ".png", ".webp");

    private final Path uploadRootPath;
    private final String publicUploadBasePath;
    private final String legacyPublicUploadBasePath;
    private final long maxImageSizeBytes;

    public FileStorageServiceImpl(
            @Value("${app.upload.dir:uploads}") String uploadDir,
            @Value("${server.servlet.context-path:}") String contextPath,
            @Value("${app.upload.max-image-size-bytes:2097152}") long maxImageSizeBytes) {
        this.uploadRootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        this.publicUploadBasePath = "/uploads";
        this.legacyPublicUploadBasePath = normalizeContextPath(contextPath) + "/uploads";
        this.maxImageSizeBytes = maxImageSizeBytes > 0 ? maxImageSizeBytes : DEFAULT_MAX_IMAGE_SIZE_BYTES;
    }

    @Override
    public FileUploadResponse storeImage(MultipartFile file, String folder) {
        validateFile(file);
        String safeFolder = sanitizeFolder(folder);
        String extension = extractExtension(file.getOriginalFilename());
        String storedFileName = generateStoredFileName(extension);
        Path targetDirectory = uploadRootPath.resolve(safeFolder).normalize();
        Path targetFile = targetDirectory.resolve(storedFileName).normalize();

        if (!targetFile.startsWith(targetDirectory)) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_INVALID_PATH);
        }

        try {
            Files.createDirectories(targetDirectory);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            log.error("Failed to store file in folder {}", safeFolder, exception);
            throw new DataException(
                    ErrorCode.UNEXPECTED_ERROR,
                    new Object[]{MessageUtils.getMessage(Constants.MessageKey.UPLOAD_STORE_FAILED)}
            );
        }

        return FileUploadResponse.builder()
                .fileName(storedFileName)
                .url(publicUploadBasePath + "/" + safeFolder + "/" + storedFileName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .build();
    }

    @Override
    public void deleteImageByUrl(String fileUrl) {
        Path filePath = resolveManagedFilePath(fileUrl, true);
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (!deleted) {
                throw invalidUploadParameter(Constants.MessageKey.UPLOAD_FILE_NOT_FOUND);
            }
        } catch (IOException exception) {
            log.error("Failed to delete uploaded file {}", fileUrl, exception);
            throw new DataException(
                    ErrorCode.UNEXPECTED_ERROR,
                    new Object[]{MessageUtils.getMessage(Constants.MessageKey.UPLOAD_DELETE_FAILED)}
            );
        }
    }

    @Override
    public void deleteManagedImageIfExists(String fileUrl) {
        if (!StringUtils.hasText(fileUrl)) {
            return;
        }
        try {
            Path filePath = resolveManagedFilePath(fileUrl, false);
            if (filePath != null) {
                Files.deleteIfExists(filePath);
            }
        } catch (IOException exception) {
            log.warn("Failed to delete managed image {}", fileUrl, exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_FILE_REQUIRED);
        }

        if (file.getSize() > maxImageSizeBytes) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_IMAGE_SIZE_EXCEEDED, maxImageSizeBytes);
        }

        String contentType = file.getContentType();
        if (!StringUtils.hasText(contentType) || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_IMAGE_INVALID_TYPE);
        }

        String extension = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_IMAGE_EXTENSION_UNSUPPORTED);
        }
    }

    private String extractExtension(String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_ORIGINAL_FILENAME_MISSING);
        }

        int extensionIndex = originalFilename.lastIndexOf('.');
        if (extensionIndex < 0 || extensionIndex == originalFilename.length() - 1) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_FILE_EXTENSION_MISSING);
        }
        return originalFilename.substring(extensionIndex).toLowerCase(Locale.ROOT);
    }

    private String generateStoredFileName(String extension) {
        return UUID.randomUUID().toString().replace("-", "") + extension;
    }

    private String sanitizeFolder(String folder) {
        if (!StringUtils.hasText(folder)) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_FOLDER_REQUIRED);
        }
        return folder.replace("\\", "/").replace("../", "").replace("..", "").replaceAll("^/+", "").replaceAll("/+$", "");
    }

    private DataException invalidUploadParameter(String messageKey, Object... args) {
        return new DataException(
                ErrorCode.INVALID_PARAMETER,
                new Object[]{MessageUtils.getMessage(messageKey, args)}
        );
    }

    private Path resolveManagedFilePath(String fileUrl, boolean strict) {
        if (!StringUtils.hasText(fileUrl)) {
            if (strict) {
                throw invalidUploadParameter(Constants.MessageKey.UPLOAD_URL_REQUIRED);
            }
            return null;
        }

        String normalizedUrl = extractPath(fileUrl.trim());
        String relativePath = extractManagedRelativePath(normalizedUrl, strict);
        if (relativePath == null) {
            return null;
        }
        if (!StringUtils.hasText(relativePath)) {
            if (strict) {
                throw invalidUploadParameter(Constants.MessageKey.UPLOAD_URL_INVALID);
            }
            return null;
        }

        Path resolvedPath = uploadRootPath.resolve(relativePath).normalize();
        if (!resolvedPath.startsWith(uploadRootPath)) {
            if (strict) {
                throw invalidUploadParameter(Constants.MessageKey.UPLOAD_INVALID_PATH);
            }
            return null;
        }
        return resolvedPath;
    }

    private String extractManagedRelativePath(String normalizedUrl, boolean strict) {
        if (!StringUtils.hasText(normalizedUrl)) {
            if (strict) {
                throw invalidUploadParameter(Constants.MessageKey.UPLOAD_URL_INVALID);
            }
            return null;
        }

        String normalizedPath = normalizedUrl.replace("\\", "/").trim();
        List<String> acceptedPrefixes = new ArrayList<>();
        acceptedPrefixes.add(publicUploadBasePath + "/");
        if (StringUtils.hasText(legacyPublicUploadBasePath)) {
            acceptedPrefixes.add(legacyPublicUploadBasePath + "/");
        }

        for (String prefix : acceptedPrefixes) {
            if (normalizedPath.startsWith(prefix)) {
                return normalizedPath.substring(prefix.length());
            }
        }

        if (strict) {
            throw invalidUploadParameter(Constants.MessageKey.UPLOAD_URL_INVALID);
        }
        return null;
    }

    private String extractPath(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            try {
                return new URI(url).getPath();
            } catch (URISyntaxException exception) {
                throw invalidUploadParameter(Constants.MessageKey.UPLOAD_URL_INVALID);
            }
        }
        return url;
    }

    private String normalizeContextPath(String contextPath) {
        if (!StringUtils.hasText(contextPath) || "/".equals(contextPath.trim())) {
            return "";
        }
        String normalized = contextPath.trim();
        return normalized.endsWith("/") ? normalized.substring(0, normalized.length() - 1) : normalized;
    }
}
