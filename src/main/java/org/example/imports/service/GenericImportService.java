package org.example.imports.service;

import org.example.imports.model.ImportExecutionResult;
import org.springframework.web.multipart.MultipartFile;

public interface GenericImportService {
    <T> ImportExecutionResult importFile(
            MultipartFile file,
            Class<T> dtoClass,
            ImportRowPersister<T> rowPersister
    );
}
