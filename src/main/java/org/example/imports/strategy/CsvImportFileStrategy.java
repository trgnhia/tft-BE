package org.example.imports.strategy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.example.imports.model.ImportRow;
import org.example.imports.model.ParsedImportFile;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CsvImportFileStrategy implements ImportFileStrategy {
    private static final String ERROR_DETAILS = "Error Details";

    @Override
    public boolean supports(String extension) {
        return "csv".equalsIgnoreCase(extension);
    }

    @Override
    public ParsedImportFile parse(InputStream inputStream) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
             CSVParser parser = new CSVParser(reader, format)) {
            List<String> headers = parser.getHeaderNames();
            if (headers == null || headers.isEmpty()) {
                throw new IllegalArgumentException("CSV file does not contain a header row.");
            }

            List<ImportRow> rows = new ArrayList<>();
            for (CSVRecord record : parser) {
                List<String> values = new ArrayList<>(headers.size());
                for (String header : headers) {
                    values.add(record.isMapped(header) ? record.get(header) : "");
                }
                rows.add(new ImportRow(record.getRecordNumber() + 1, values));
            }
            return new ParsedImportFile(headers, rows);
        }
    }

    @Override
    public byte[] buildErrorFile(ParsedImportFile parsedFile, Map<Long, String> errorsByRowNumber) throws IOException {
        List<String> outputHeaders = new ArrayList<>(parsedFile.headers());
        outputHeaders.add(ERROR_DETAILS);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(outputHeaders);

            for (ImportRow row : parsedFile.rows()) {
                List<String> outputValues = new ArrayList<>(row.values());
                outputValues.add(errorsByRowNumber.getOrDefault(row.rowNumber(), ""));
                printer.printRecord(outputValues);
            }

            printer.flush();
            return out.toByteArray();
        }
    }

    @Override
    public String outputContentType() {
        return "text/csv";
    }

    @Override
    public String outputFileExtension() {
        return "csv";
    }
}
