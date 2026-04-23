package org.example.dto.champs;

import lombok.Builder;

import java.util.List;

@Builder
public record ChampImportResultResponse(
        int totalRows,
        int successRows,
        int failedRows,
        int insertedRows,
        int updatedRows,
        int warningRows,
        int errorRows,
        String message,
        List<ChampImportRowErrorResponse> rowErrors,
        String resultFileName,
        String resultFileContentType,
        String resultFileBase64
) {
}
