package org.example.imports.strategy;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.example.imports.model.ImportRow;
import org.example.imports.model.ImportRowReport;
import org.example.imports.model.ParsedImportFile;
import org.example.imports.util.ImportHeaderUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.util.MessageUtils.getMessage;

@Component
public class CsvImportFileStrategy implements ImportFileStrategy {
    private static final String STATUS_CANONICAL = "Status";
    private static final String MESSAGE_CANONICAL = "Message";

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
                throw new IllegalArgumentException(getMessage("import.csv.error.missing_header_row"));
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
    public byte[] buildResultFile(
            ParsedImportFile parsedFile,
            Map<Long, ImportRowReport> rowReportsByRowNumber
    ) throws IOException {
        List<ColumnBinding> dataColumns = resolveDataColumns(parsedFile.headers());
        List<String> outputHeaders = dataColumns.stream().map(ColumnBinding::header).toList();
        List<String> headersWithStatus = new ArrayList<>(outputHeaders);
        headersWithStatus.add(resolveStatusColumnHeader());
        headersWithStatus.add(resolveMessageColumnHeader());

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             Writer writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
            printer.printRecord(headersWithStatus);

            for (ImportRow row : parsedFile.rows()) {
                List<String> outputValues = new ArrayList<>(dataColumns.size() + 2);
                for (ColumnBinding column : dataColumns) {
                    String value = column.index() < row.values().size() ? row.values().get(column.index()) : "";
                    outputValues.add(value == null ? "" : value);
                }

                ImportRowReport report = rowReportsByRowNumber.get(row.rowNumber());
                outputValues.add(report != null ? localizeStatus(report.status()) : "");
                outputValues.add(report != null ? report.message() : "");
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

    private List<ColumnBinding> resolveDataColumns(List<String> headers) {
        List<ColumnBinding> columns = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            String header = headers.get(i);
            if (ImportHeaderUtils.isSystemResultColumn(header)) {
                continue;
            }
            columns.add(new ColumnBinding(i, header));
        }
        return columns;
    }

    private String resolveStatusColumnHeader() {
        String key = "champ.import.result.columns.status";
        String localized = getMessage(key);
        if (key.equals(localized)) {
            localized = STATUS_CANONICAL;
        }
        return ImportHeaderUtils.composeLocalizedHeader(STATUS_CANONICAL, localized);
    }

    private String resolveMessageColumnHeader() {
        String key = "champ.import.result.columns.message";
        String localized = getMessage(key);
        if (key.equals(localized)) {
            localized = MESSAGE_CANONICAL;
        }
        return ImportHeaderUtils.composeLocalizedHeader(MESSAGE_CANONICAL, localized);
    }

    private String localizeStatus(String statusCode) {
        if (statusCode == null || statusCode.isBlank()) {
            return "";
        }

        String normalized = statusCode.trim().toUpperCase();
        String key = switch (normalized) {
            case "SUCCESS_INSERT" -> "champ.import.result.status.success_insert";
            case "SUCCESS_UPDATE" -> "champ.import.result.status.success_update";
            case "WARNING_INSERT" -> "champ.import.result.status.warning_insert";
            case "WARNING_UPDATE" -> "champ.import.result.status.warning_update";
            case "WARNING" -> "champ.import.result.status.warning";
            case "ERROR" -> "champ.import.result.status.error";
            case "SUCCESS" -> "champ.import.result.status.success";
            default -> statusCode;
        };

        if (key.equals(statusCode)) {
            return statusCode;
        }
        String resolved = getMessage(key);
        return key.equals(resolved) ? statusCode : resolved;
    }

    private record ColumnBinding(int index, String header) {
    }
}
