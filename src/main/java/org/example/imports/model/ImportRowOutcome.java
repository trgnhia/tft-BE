package org.example.imports.model;

public record ImportRowOutcome(
        String status,
        String message,
        boolean error
) {
    public static ImportRowOutcome success(String status, String message) {
        return new ImportRowOutcome(status, message, false);
    }

    public static ImportRowOutcome warning(String message) {
        return new ImportRowOutcome("WARNING", message, false);
    }

    public static ImportRowOutcome error(String message) {
        return new ImportRowOutcome("ERROR", message, true);
    }
}
