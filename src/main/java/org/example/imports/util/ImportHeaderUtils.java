package org.example.imports.util;

import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Set;

public final class ImportHeaderUtils {

    private static final Set<String> SYSTEM_RESULT_COLUMNS = Set.of(
            "status",
            "message",
            "error details"
    );

    private ImportHeaderUtils() {
    }

    public static String extractCanonicalHeader(String header) {
        if (!StringUtils.hasText(header)) {
            return "";
        }

        String trimmed = stripUtf8Bom(header).trim();
        int openParenthesis = trimmed.indexOf('(');
        int closeParenthesis = trimmed.lastIndexOf(')');
        if (openParenthesis > 0 && closeParenthesis > openParenthesis) {
            return trimmed.substring(0, openParenthesis).trim();
        }
        return trimmed;
    }

    public static String normalizeHeader(String header) {
        String canonical = extractCanonicalHeader(header);
        return canonical.toLowerCase(Locale.ROOT).trim();
    }

    public static boolean isSystemResultColumn(String header) {
        return SYSTEM_RESULT_COLUMNS.contains(normalizeHeader(header));
    }

    public static String composeLocalizedHeader(String canonicalHeader, String localizedLabel) {
        String canonical = canonicalHeader == null ? "" : canonicalHeader.trim();
        String localized = localizedLabel == null ? "" : localizedLabel.trim();

        if (!StringUtils.hasText(canonical)) {
            return localized;
        }
        if (!StringUtils.hasText(localized) || canonical.equalsIgnoreCase(localized)) {
            return canonical;
        }
        return canonical + " (" + localized + ")";
    }

    private static String stripUtf8Bom(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        return value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
    }
}
