package org.example.dto.champs;

public record ChampImportTemplateFile(
        byte[] content,
        String fileName,
        String contentType
) {
}
