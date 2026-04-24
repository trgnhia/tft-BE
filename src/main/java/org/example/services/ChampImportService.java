package org.example.services;

import org.example.dto.champs.ChampImportTemplateFile;
import org.example.imports.model.ImportExecutionResult;
import org.springframework.web.multipart.MultipartFile;

public interface ChampImportService {
    ImportExecutionResult importChamps(MultipartFile file);

    ChampImportTemplateFile downloadTemplate(String format);
}
