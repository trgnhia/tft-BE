package org.example.imports.strategy;

import org.example.imports.model.ImportRowReport;
import org.example.imports.model.ParsedImportFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ImportFileStrategy {
    boolean supports(String extension);

    ParsedImportFile parse(InputStream inputStream) throws IOException;

    byte[] buildResultFile(ParsedImportFile parsedFile, Map<Long, ImportRowReport> rowReportsByRowNumber) throws IOException;

    String outputContentType();

    String outputFileExtension();
}
