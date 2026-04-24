package org.example.dto.champs;

import lombok.Builder;

import java.util.List;

@Builder
public record ChampImportRowErrorResponse(
        long rowNumber,
        List<String> errors
) {
}
