package org.example.dto.champs;

import lombok.Builder;

import java.util.List;

@Builder
public record ChampImportResultResponse(
        int totalRows,
        int successRows,
        int failedRows,
        String message,
        List<ChampImportRowErrorResponse> rowErrors
) {
}
