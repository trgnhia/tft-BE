package org.example.imports.strategy;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.imports.model.ImportRow;
import org.example.imports.model.ParsedImportFile;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ExcelImportFileStrategy implements ImportFileStrategy {

    private static final String ERROR_DETAILS = "Error Details";

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
                throw new IllegalArgumentException("Excel file does not contain a header row.");
            }

            DataFormatter formatter = new DataFormatter();
            int headerSize = validateAndGetHeaderSize(headerRow);
            List<String> headers = extractHeaders(headerRow, headerSize, formatter);
            List<ImportRow> rows = extractRows(sheet, firstRowNum, headerSize, formatter);

            return new ParsedImportFile(headers, rows);
        }
    }

    @Override
    public byte[] buildErrorFile(ParsedImportFile parsedFile, Map<Long, String> errorsByRowNumber) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Import Result");
            List<String> headers = parsedFile.headers();

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.size(); col++) {
                headerRow.createCell(col).setCellValue(headers.get(col));
            }
            headerRow.createCell(headers.size()).setCellValue(ERROR_DETAILS);

            writeFailedRows(parsedFile, errorsByRowNumber, sheet, headers.size());

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
            throw new IllegalArgumentException("Excel header row is empty.");
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

    private void writeFailedRows(
            ParsedImportFile parsedFile,
            Map<Long, String> errorsByRowNumber,
            Sheet sheet,
            int errorColumnIndex
    ) {
        int rowIndex = 1;
        for (ImportRow sourceRow : parsedFile.rows()) {
            String errorDetail = errorsByRowNumber.get(sourceRow.rowNumber());
            if (errorDetail != null) {
                Row targetRow = sheet.createRow(rowIndex++);
                for (int col = 0; col < sourceRow.values().size(); col++) {
                    targetRow.createCell(col).setCellValue(sourceRow.values().get(col));
                }
                targetRow.createCell(errorColumnIndex).setCellValue(errorDetail);
            }
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
}
