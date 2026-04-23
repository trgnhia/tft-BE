package org.example.imports.strategy;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.imports.model.ImportRow;
import org.example.imports.model.ImportRowReport;
import org.example.imports.model.ParsedImportFile;
import org.example.imports.util.ImportHeaderUtils;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.util.MessageUtils.getMessage;

@Component
public class ExcelImportFileStrategy implements ImportFileStrategy {

    private static final String STATUS_CANONICAL = "Status";
    private static final String MESSAGE_CANONICAL = "Message";
    private static final String RESULT_SHEET_DEFAULT = "Import Result";

    @Override
    public boolean supports(String extension) {
        return "xlsx".equalsIgnoreCase(extension);
    }

    @Override
    public ParsedImportFile parse(InputStream inputStream) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            int firstRowNum = sheet.getFirstRowNum();

            Row headerRow = sheet.getRow(firstRowNum);
            if (headerRow == null) {
                throw new IllegalArgumentException(getMessage("import.excel.error.missing_header_row"));
            }

            DataFormatter formatter = new DataFormatter();
            int headerSize = validateAndGetHeaderSize(headerRow);
            List<String> headers = extractHeaders(headerRow, headerSize, formatter);
            List<ImportRow> rows = extractRows(sheet, firstRowNum, headerSize, formatter);

            return new ParsedImportFile(headers, rows);
        }
    }

    @Override
    public byte[] buildResultFile(
            ParsedImportFile parsedFile,
            Map<Long, ImportRowReport> rowReportsByRowNumber
    ) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(resolveResultSheetName());
            List<ColumnBinding> columns = resolveDataColumns(parsedFile.headers());

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < columns.size(); col++) {
                headerRow.createCell(col).setCellValue(columns.get(col).header());
            }
            int statusColumnIndex = columns.size();
            int messageColumnIndex = statusColumnIndex + 1;
            headerRow.createCell(statusColumnIndex).setCellValue(resolveStatusColumnHeader());
            headerRow.createCell(messageColumnIndex).setCellValue(resolveMessageColumnHeader());

            writeAllRows(parsedFile, rowReportsByRowNumber, sheet, columns, statusColumnIndex, messageColumnIndex);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    @Override
    public String outputContentType() {
        return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    }

    @Override
    public String outputFileExtension() {
        return "xlsx";
    }

    private int validateAndGetHeaderSize(Row headerRow) {
        int headerSize = headerRow.getLastCellNum();
        if (headerSize <= 0) {
            throw new IllegalArgumentException(getMessage("import.excel.error.empty_header_row"));
        }
        return headerSize;
    }

    private List<String> extractHeaders(Row headerRow, int headerSize, DataFormatter formatter) {
        List<String> headers = new ArrayList<>(headerSize);
        for (int i = 0; i < headerSize; i++) {
            headers.add(formatter.formatCellValue(headerRow.getCell(i)).trim());
        }
        return headers;
    }

    private List<ImportRow> extractRows(
            Sheet sheet,
            int firstRowNum,
            int headerSize,
            DataFormatter formatter
    ) {
        List<ImportRow> rows = new ArrayList<>();
        for (int rowIndex = firstRowNum + 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
            Row row = sheet.getRow(rowIndex);
            if (!isEmptyRow(row, headerSize, formatter)) {
                rows.add(new ImportRow(rowIndex + 1L, readRowValues(row, headerSize, formatter)));
            }
        }
        return rows;
    }

    private List<String> readRowValues(Row row, int headerSize, DataFormatter formatter) {
        List<String> values = new ArrayList<>(headerSize);
        for (int col = 0; col < headerSize; col++) {
            String value = row == null ? "" : formatter.formatCellValue(row.getCell(col));
            values.add(value == null ? "" : value.trim());
        }
        return values;
    }

    private void writeAllRows(
            ParsedImportFile parsedFile,
            Map<Long, ImportRowReport> rowReportsByRowNumber,
            Sheet sheet,
            List<ColumnBinding> columns,
            int statusColumnIndex,
            int messageColumnIndex
    ) {
        int rowIndex = 1;
        for (ImportRow sourceRow : parsedFile.rows()) {
            Row targetRow = sheet.createRow(rowIndex++);
            for (int col = 0; col < columns.size(); col++) {
                int sourceIndex = columns.get(col).index();
                String value = sourceIndex < sourceRow.values().size() ? sourceRow.values().get(sourceIndex) : "";
                targetRow.createCell(col).setCellValue(value == null ? "" : value);
            }

            ImportRowReport report = rowReportsByRowNumber.get(sourceRow.rowNumber());
            targetRow.createCell(statusColumnIndex).setCellValue(report != null ? localizeStatus(report.status()) : "");
            targetRow.createCell(messageColumnIndex).setCellValue(report != null ? report.message() : "");
        }
    }

    private boolean isEmptyRow(Row row, int headerSize, DataFormatter formatter) {
        if (row == null) return true;
        for (int col = 0; col < headerSize; col++) {
            String value = formatter.formatCellValue(row.getCell(col));
            if (value != null && !value.trim().isEmpty()) {
                return false;
            }
        }
        return true;
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

    private String resolveResultSheetName() {
        String key = "champ.import.result.sheet_name";
        String resolved = getMessage(key);
        return key.equals(resolved) ? RESULT_SHEET_DEFAULT : resolved;
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
