package org.example.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utility class focused on java.time.Instant usage.
 * Implements the "UTC Everywhere" best practice for Modern Java Backends.
 * Thread-safe and immutable.
 */
public final class DateUtil {

    // Common Time Zones
    public static final ZoneId ZONE_UTC = ZoneId.of("UTC");
    public static final ZoneId ZONE_VN = ZoneId.of("Asia/Ho_Chi_Minh"); // Example local zone

    // Standard ISO-8601 formatter (yyyy-MM-ddTHH:mm:ssZ) for API responses
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT;

    // Private constructor to prevent instantiation
    private DateUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    // ========================================================================
    // 1. GET CURRENT TIME
    // ========================================================================

    /**
     * Gets the current timestamp in UTC.
     * @return Current Instant (UTC)
     */
    public static Instant now() {
        return Instant.now();
    }

    // ========================================================================
    // 2. FORMATTING (Instant -> String)
    // ========================================================================

    /**
     * Formats Instant to ISO-8601 standard string (ends with 'Z').
     * Example Output: "2023-10-25T14:30:00Z"
     * * @param instant The instant to format
     * @return ISO-8601 string or null if input is null
     */
    public static String toString(Instant instant) {
        if (instant == null) return null;
        return ISO_FORMATTER.format(instant);
    }

    /**
     * Formats Instant to a custom pattern string based on a specific Time Zone.
     * Example: format(now, "dd/MM/yyyy HH:mm", ZONE_VN)
     * * @param instant The instant to format
     * @param pattern The date pattern (e.g., "yyyy-MM-dd")
     * @param zoneId The target time zone for display
     * @return Formatted string
     */
    public static String format(Instant instant, String pattern, ZoneId zoneId) {
        if (instant == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern).withZone(zoneId);
        return formatter.format(instant);
    }

    /**
     * Formats Instant to a custom pattern string in UTC.
     * * @param instant The instant to format
     * @param pattern The date pattern
     * @return Formatted string in UTC
     */
    public static String formatUTC(Instant instant, String pattern) {
        return format(instant, pattern, ZONE_UTC);
    }

    // ========================================================================
    // 3. PARSING (String -> Instant)
    // ========================================================================

    /**
     * Parses an ISO-8601 standard string (must contain 'Z' or offset) to Instant.
     * Example Input: "2023-10-25T14:30:00Z"
     * * @param isoString The ISO string
     * @return Instant object or null if parsing fails
     */
    public static Instant parseIso(String isoString) {
        if (isoString == null || isoString.isBlank()) return null;
        try {
            return Instant.parse(isoString);
        } catch (DateTimeParseException e) {
            // Consider logging the error here
            return null;
        }
    }

    /**
     * Parses a local date string (without timezone info) to an Instant,
     * assuming it belongs to a specific Time Zone.
     * * Example: Input "2023-10-25 07:00:00" (VN Time) -> Saved as Instant (UTC)
     * * @param dateStr The date string (e.g., "2023-10-25 07:00:00")
     * @param pattern The pattern of the input string
     * @param originZone The time zone where this date string originated
     * @return Instant (converted to UTC)
     */
    public static Instant parseFromLocalString(String dateStr, String pattern, ZoneId originZone) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            // Parse to LocalDateTime first (no zone info)
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
            // Attach zone and convert to Instant (UTC)
            return localDateTime.atZone(originZone).toInstant();
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    // ========================================================================
    // 4. CONVERSION (Legacy Date / Timestamp <-> Instant)
    // ========================================================================

    /**
     * Converts java.util.Date to Instant.
     */
    public static Instant toInstant(Date date) {
        return (date == null) ? null : date.toInstant();
    }

    /**
     * Converts Instant to java.util.Date.
     */
    public static Date toDate(Instant instant) {
        return (instant == null) ? null : Date.from(instant);
    }

    /**
     * Creates Instant from epoch milliseconds.
     */
    public static Instant fromEpochMilli(long millis) {
        return Instant.ofEpochMilli(millis);
    }

    /**
     * Converts Instant to epoch milliseconds.
     */
    public static long toEpochMilli(Instant instant) {
        return (instant == null) ? 0 : instant.toEpochMilli();
    }

    // ========================================================================
    // 5. CALCULATIONS (Start/End of Day)
    // ========================================================================

    /* * NOTE: "Start of Day" or "End of Day" depends entirely on the Time Zone.
     * 00:00 in Vietnam is different from 00:00 in New York.
     */

    /**
     * Calculates the start of the day (00:00:00) for a specific Time Zone,
     * returning the result as an Instant (UTC).
     * * @param instant The reference time
     * @param zoneId The target time zone
     * @return Instant representing 00:00:00 in the given zone
     */
    public static Instant startOfDay(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return instant.atZone(zoneId)
                .toLocalDate()
                .atStartOfDay(zoneId)
                .toInstant();
    }

    /**
     * Calculates the end of the day (23:59:59.999...) for a specific Time Zone,
     * returning the result as an Instant (UTC).
     * * @param instant The reference time
     * @param zoneId The target time zone
     * @return Instant representing end of day in the given zone
     */
    public static Instant endOfDay(Instant instant, ZoneId zoneId) {
        if (instant == null) return null;
        return instant.atZone(zoneId)
                .toLocalDate()
                .atTime(LocalTime.MAX)
                .atZone(zoneId)
                .toInstant();
    }

    /**
     * Adds days to an Instant.
     */
    public static Instant plusDays(Instant instant, long days) {
        return (instant == null) ? null : instant.plus(days, ChronoUnit.DAYS);
    }

    /**
     * Subtracts days from an Instant.
     */
    public static Instant minusDays(Instant instant, long days) {
        return (instant == null) ? null : instant.minus(days, ChronoUnit.DAYS);
    }
}
