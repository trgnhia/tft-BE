package org.example.imports.model;

import java.util.List;

public record ParsedImportFile(List<String> headers, List<ImportRow> rows) {
}
