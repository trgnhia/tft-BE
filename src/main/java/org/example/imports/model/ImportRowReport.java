package org.example.imports.model;

public record ImportRowReport(
        long rowNumber,
        String status,
        String message
) {
}
