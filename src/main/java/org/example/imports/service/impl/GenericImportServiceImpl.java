package org.example.imports.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.annotations.ImportColumn;
import org.example.common.constant.Constants;
import org.example.common.exception.ServerException;
import org.example.imports.model.ImportExecutionResult;
import org.example.imports.model.ImportRow;
import org.example.imports.model.ImportRowError;
import org.example.imports.model.ImportRowOutcome;
import org.example.imports.model.ImportRowReport;
import org.example.imports.model.ParsedImportFile;
import org.example.imports.service.GenericImportService;
import org.example.imports.service.ImportRowPersister;
import org.example.imports.service.ImportRowPersisterWithOutcome;
import org.example.imports.strategy.ImportFileStrategy;
import org.example.imports.util.ImportHeaderUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericImportServiceImpl implements GenericImportService {

    private static final int MAX_ROWS = 50_000;
    private static final String DEFAULT_SUCCESS_STATUS = "SUCCESS";
    private static final String WARNING_STATUS = "WARNING";
    private static final String ERROR_STATUS = "ERROR";

    private static final Map<Class<?>, Function<String, Object>> SIMPLE_CONVERTERS = Map.ofEntries(
            Map.entry(String.class, value -> value),
            Map.entry(Integer.class, Integer::parseInt),
            Map.entry(int.class, Integer::parseInt),
            Map.entry(Long.class, Long::parseLong),
            Map.entry(long.class, Long::parseLong),
            Map.entry(Double.class, Double::parseDouble),
            Map.entry(double.class, Double::parseDouble),
            Map.entry(Float.class, Float::parseFloat),
            Map.entry(float.class, Float::parseFloat),
            Map.entry(BigDecimal.class, BigDecimal::new),
            Map.entry(Boolean.class, GenericImportServiceImpl::parseBoolean),
            Map.entry(boolean.class, GenericImportServiceImpl::parseBoolean)
    );

    private final List<ImportFileStrategy> fileStrategies;
    private final Validator validator;

    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private DataSize maxFileSize;

    @Override
    public <T> ImportExecutionResult importFile(
            MultipartFile file,
            Class<T> dtoClass,
            ImportRowPersister<T> rowPersister
    ) {
        return importFileWithOutcome(file, dtoClass, rowDto -> {
            rowPersister.persist(rowDto);
            return ImportRowOutcome.success(DEFAULT_SUCCESS_STATUS, getMessage("import.row.message.imported_successfully"));
        });
    }

    @Override
    public <T> ImportExecutionResult importFileWithOutcome(
            MultipartFile file,
            Class<T> dtoClass,
            ImportRowPersisterWithOutcome<T> rowPersister
    ) {
        validateFile(file);
        String extension = extractExtension(file.getOriginalFilename());
        ImportFileStrategy strategy = resolveStrategy(extension);
        ParsedImportFile parsedFile = readFile(file, strategy);

        log.info("Import started: file={}, rows={}", file.getOriginalFilename(), parsedFile.rows().size());

        if (parsedFile.rows().isEmpty()) {
            log.info("Import completed: file={}, success=0", file.getOriginalFilename());
            return ImportExecutionResult.empty();
        }

        ensureRowLimit(parsedFile.rows().size());

        List<FieldBinding> fieldBindings = resolveFieldBindings(dtoClass, parsedFile.headers());
        Map<Long, List<String>> rowErrors = new LinkedHashMap<>();
        Map<Long, ImportRowReport> rowReportsByRow = new LinkedHashMap<>();

        List<ValidRow<T>> validRows = collectValidRows(
                dtoClass,
                fieldBindings,
                parsedFile.rows(),
                rowErrors,
                rowReportsByRow
        );

        PersistSummary persistSummary = persistValidRows(
                rowPersister,
                validRows,
                rowErrors,
                rowReportsByRow
        );

        int failedCount = rowErrors.size();
        int warningCount = persistSummary.warningCount();
        int successCount = persistSummary.successCount();

        log.info(
                "Import completed: file={}, success={}, warning={}, failed={}",
                file.getOriginalFilename(),
                successCount,
                warningCount,
                failedCount
        );

        List<ImportRowReport> rowReports = rowReportsByRow.values().stream()
                .sorted((left, right) -> Long.compare(left.rowNumber(), right.rowNumber()))
                .toList();

        if (failedCount == 0 && warningCount == 0) {
            return ImportExecutionResult.success(parsedFile.rows().size(), successCount, rowReports);
        }

        List<ImportRowError> rowErrorDetails = buildRowErrors(rowErrors);
        byte[] resultFileContent = writeResultFile(strategy, parsedFile, rowReportsByRow);
        String resultFileName = buildResultFileName(file.getOriginalFilename(), strategy.outputFileExtension());

        return ImportExecutionResult.withIssues(
                parsedFile.rows().size(),
                successCount,
                failedCount,
                warningCount,
                rowErrorDetails,
                rowReports,
                resultFileContent,
                resultFileName,
                strategy.outputContentType()
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_FILE_EMPTY));
        }
        if (file.getSize() > maxFileSize.toBytes()) {
            throw new IllegalArgumentException(
                    getMessage(Constants.MessageKey.IMPORT_FILE_SIZE_EXCEEDED, maxFileSize)
            );
        }
    }

    private void ensureRowLimit(int rowSize) {
        if (rowSize > MAX_ROWS) {
            throw new IllegalArgumentException(
                    getMessage(Constants.MessageKey.IMPORT_ROW_LIMIT_EXCEEDED, MAX_ROWS)
            );
        }
    }

    private <T> List<ValidRow<T>> collectValidRows(
            Class<T> dtoClass,
            List<FieldBinding> fieldBindings,
            List<ImportRow> rows,
            Map<Long, List<String>> rowErrors,
            Map<Long, ImportRowReport> rowReportsByRow
    ) {
        List<ValidRow<T>> validRows = new ArrayList<>();
        for (ImportRow row : rows) {
            BindingResult<T> result = bindAndValidateRow(dtoClass, fieldBindings, row);
            if (result.errors().isEmpty()) {
                validRows.add(new ValidRow<>(row.rowNumber(), result.dto()));
            } else {
                log.debug("Row {} failed validation: {}", row.rowNumber(), result.errors());
                addRowErrors(rowErrors, row.rowNumber(), result.errors());
                rowReportsByRow.put(
                        row.rowNumber(),
                        new ImportRowReport(row.rowNumber(), ERROR_STATUS, joinMessages(result.errors()))
                );
            }
        }
        return validRows;
    }

    private <T> PersistSummary persistValidRows(
            ImportRowPersisterWithOutcome<T> rowPersister,
            List<ValidRow<T>> validRows,
            Map<Long, List<String>> rowErrors,
            Map<Long, ImportRowReport> rowReportsByRow
    ) {
        int successCount = 0;
        int warningCount = 0;

        for (ValidRow<T> validRow : validRows) {
            try {
                ImportRowOutcome outcome = rowPersister.persist(validRow.dto());
                ImportRowOutcome normalizedOutcome = normalizeOutcome(outcome);

                if (normalizedOutcome.error() || ERROR_STATUS.equalsIgnoreCase(normalizedOutcome.status())) {
                    List<String> errors = splitMessages(normalizedOutcome.message());
                    addRowErrors(rowErrors, validRow.rowNumber(), errors);
                    rowReportsByRow.put(
                            validRow.rowNumber(),
                            new ImportRowReport(validRow.rowNumber(), ERROR_STATUS, joinMessages(errors))
                    );
                    continue;
                }

                String status = normalizedOutcome.status().toUpperCase(Locale.ROOT);
                String message = messageOrDefault(
                        normalizedOutcome.message(),
                        getMessage("import.row.message.imported_successfully")
                );

                if (isWarningStatus(status)) {
                    warningCount++;
                }

                successCount++;
                rowReportsByRow.put(validRow.rowNumber(), new ImportRowReport(validRow.rowNumber(), status, message));
            } catch (RuntimeException ex) {
                log.warn("Row {} failed to persist", validRow.rowNumber(), ex);
                List<String> errors = extractExceptionMessages(ex);
                addRowErrors(rowErrors, validRow.rowNumber(), errors);
                rowReportsByRow.put(
                        validRow.rowNumber(),
                        new ImportRowReport(validRow.rowNumber(), ERROR_STATUS, joinMessages(errors))
                );
            }
        }

        return new PersistSummary(successCount, warningCount);
    }

    private ImportRowOutcome normalizeOutcome(ImportRowOutcome outcome) {
        if (outcome == null) {
            return ImportRowOutcome.success(DEFAULT_SUCCESS_STATUS, getMessage("import.row.message.imported_successfully"));
        }
        String status = messageOrDefault(outcome.status(), DEFAULT_SUCCESS_STATUS).toUpperCase(Locale.ROOT);
        String message = messageOrDefault(outcome.message(), getMessage("import.row.message.imported_successfully"));
        if (outcome.error() || ERROR_STATUS.equals(status)) {
            return ImportRowOutcome.error(message);
        }
        return new ImportRowOutcome(status, message, false);
    }

    private List<ImportRowError> buildRowErrors(Map<Long, List<String>> rowErrors) {
        return rowErrors.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ImportRowError(entry.getKey(), List.copyOf(entry.getValue())))
                .toList();
    }

    private ImportFileStrategy resolveStrategy(String extension) {
        return fileStrategies.stream()
                .filter(s -> s.supports(extension))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        getMessage(Constants.MessageKey.IMPORT_UNSUPPORTED_FILE_TYPE, extension))
                );
    }

    private ParsedImportFile readFile(MultipartFile file, ImportFileStrategy strategy) {
        try {
            return strategy.parse(file.getInputStream());
        } catch (IOException ex) {
            throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_READ_FILE_FAILED), ex);
        }
    }

    private byte[] writeResultFile(
            ImportFileStrategy strategy,
            ParsedImportFile parsedFile,
            Map<Long, ImportRowReport> rowReportsByRow
    ) {
        try {
            return strategy.buildResultFile(parsedFile, rowReportsByRow);
        } catch (IOException ex) {
            throw new IllegalStateException(getMessage(Constants.MessageKey.IMPORT_BUILD_ERROR_FILE_FAILED), ex);
        }
    }

    private <T> List<FieldBinding> resolveFieldBindings(Class<T> dtoClass, List<String> headers) {
        Map<String, Integer> headerIndexMap = new LinkedHashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            String rawHeader = headers.get(i);
            headerIndexMap.putIfAbsent(normalize(rawHeader), i);

            String canonicalHeader = ImportHeaderUtils.extractCanonicalHeader(rawHeader);
            if (!canonicalHeader.isBlank()) {
                headerIndexMap.putIfAbsent(normalize(canonicalHeader), i);
            }
        }

        List<String> missingColumns = new ArrayList<>();
        List<FieldBinding> fieldBindings = new ArrayList<>();

        for (Field field : dtoClass.getDeclaredFields()) {
            ImportColumn annotation = field.getAnnotation(ImportColumn.class);
            if (annotation != null) {
                Integer columnIndex = headerIndexMap.get(normalize(annotation.name()));
                if (columnIndex == null) {
                    missingColumns.add(annotation.name());
                } else {
                    String resolvedColumnName = columnIndex < headers.size()
                            ? headers.get(columnIndex)
                            : annotation.name();
                    fieldBindings.add(
                            new FieldBinding(
                                    field.getName(),
                                    field.getType(),
                                    resolvedColumnName,
                                    columnIndex,
                                    annotation.required()
                            )
                    );
                }
            }
        }

        if (!missingColumns.isEmpty()) {
            throw new IllegalArgumentException(
                    getMessage(Constants.MessageKey.IMPORT_MISSING_COLUMNS, String.join(", ", missingColumns))
            );
        }
        if (fieldBindings.isEmpty()) {
            throw new IllegalArgumentException(
                    getMessage(Constants.MessageKey.IMPORT_NO_COLUMNS_ANNOTATED, dtoClass.getName())
            );
        }

        return fieldBindings;
    }

    private <T> BindingResult<T> bindAndValidateRow(
            Class<T> dtoClass,
            List<FieldBinding> fieldBindings,
            ImportRow row
    ) {
        List<String> errors = new ArrayList<>();
        T dto = createInstance(dtoClass);
        BeanWrapper beanWrapper = new BeanWrapperImpl(dto);

        for (FieldBinding binding : fieldBindings) {
            processBinding(row, errors, beanWrapper, binding);
        }

        Set<ConstraintViolation<T>> violations = validator.validate(dto);
        violations.forEach(v -> errors.add(v.getPropertyPath() + ": " + v.getMessage()));

        return new BindingResult<>(dto, errors);
    }

    private void processBinding(
            ImportRow row,
            List<String> errors,
            BeanWrapper beanWrapper,
            FieldBinding binding
    ) {
        String raw = binding.columnIndex() < row.values().size()
                ? row.values().get(binding.columnIndex())
                : null;

        if (isBlank(raw)) {
            if (binding.required()) {
                errors.add(getMessage(Constants.MessageKey.IMPORT_COLUMN_REQUIRED, binding.columnName()));
            }
            return;
        }

        String normalizedRaw = raw.trim();
        try {
            Object converted = convertValue(normalizedRaw, binding.fieldType());
            beanWrapper.setPropertyValue(binding.fieldName(), converted);
        } catch (RuntimeException ex) {
            errors.add(getMessage(Constants.MessageKey.IMPORT_COLUMN_INVALID_VALUE, binding.columnName(), normalizedRaw));
        }
    }

    private <T> T createInstance(Class<T> dtoClass) {
        try {
            return dtoClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException
                 | InvocationTargetException | NoSuchMethodException ex) {
            throw new IllegalArgumentException(
                    getMessage(Constants.MessageKey.IMPORT_DTO_NO_NOARGS, dtoClass.getName()), ex
            );
        }
    }

    private Object convertValue(String value, Class<?> targetType) {
        try {
            Function<String, Object> simpleConverter = SIMPLE_CONVERTERS.get(targetType);
            if (simpleConverter != null) {
                return simpleConverter.apply(value);
            }
            if (LocalDate.class.equals(targetType)) {
                return LocalDate.parse(value);
            }
            if (LocalDateTime.class.equals(targetType)) {
                return LocalDateTime.parse(value);
            }
            if (targetType.isEnum()) {
                return parseEnum(value, targetType);
            }
        } catch (DateTimeParseException | NumberFormatException ex) {
            throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_INVALID_VALUE_FORMAT, value), ex);
        }

        throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_UNSUPPORTED_FIELD_TYPE, targetType.getName()));
    }

    private Object parseEnum(String value, Class<?> enumType) {
        for (Object constant : enumType.getEnumConstants()) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(value.trim())) {
                return constant;
            }
        }
        String accepted = Arrays.stream(enumType.getEnumConstants())
                .map(c -> ((Enum<?>) c).name())
                .collect(Collectors.joining(", "));
        throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_INVALID_ENUM_VALUE, value, accepted));
    }

    private static Boolean parseBoolean(String rawValue) {
        return switch (rawValue.trim().toLowerCase(Locale.ROOT)) {
            case "true" -> true;
            case "false" -> false;
            default -> throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_INVALID_BOOLEAN_VALUE, rawValue));
        };
    }

    private String extractExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException(getMessage(Constants.MessageKey.IMPORT_FILE_EXTENSION_MISSING));
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private String buildResultFileName(String originalFileName, String extension) {
        String safeName = (originalFileName == null || originalFileName.isBlank())
                ? "import-file"
                : originalFileName;
        int dotIndex = safeName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? safeName.substring(0, dotIndex) : safeName;
        return baseName + "-result." + extension;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String joinMessages(List<String> messages) {
        if (messages == null || messages.isEmpty()) {
            return "";
        }
        return messages.stream()
                .filter(message -> message != null && !message.isBlank())
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining("; "));
    }

    private List<String> splitMessages(String message) {
        if (message == null || message.isBlank()) {
            return List.of(getMessage(Constants.MessageKey.IMPORT_PERSIST_GENERIC_ERROR));
        }
        return Arrays.stream(message.split(";"))
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .distinct()
                .toList();
    }

    private String messageOrDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value.trim();
    }

    private boolean isWarningStatus(String status) {
        if (status == null || status.isBlank()) {
            return false;
        }
        return status.trim().toUpperCase(Locale.ROOT).startsWith(WARNING_STATUS);
    }

    private void addRowErrors(Map<Long, List<String>> rowErrors, long rowNumber, List<String> errorsToAdd) {
        if (errorsToAdd == null || errorsToAdd.isEmpty()) {
            return;
        }
        List<String> bucket = rowErrors.computeIfAbsent(rowNumber, ignored -> new ArrayList<>());
        for (String error : errorsToAdd) {
            if (error != null && !error.isBlank() && !bucket.contains(error)) {
                bucket.add(error);
            }
        }
    }

    private List<String> extractExceptionMessages(Throwable throwable) {
        LinkedHashSet<String> messages = new LinkedHashSet<>();
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof ServerException serverException) {
                addServerExceptionMessages(messages, serverException);
            } else {
                addMessageIfPresent(messages, current.getMessage());
            }
            current = current.getCause();
        }

        if (messages.isEmpty()) {
            messages.add(getMessage(Constants.MessageKey.IMPORT_PERSIST_GENERIC_ERROR));
        }
        return new ArrayList<>(messages);
    }

    private void addServerExceptionMessages(LinkedHashSet<String> messages, ServerException serverException) {
        String[] args = serverException.getArgs();
        if (args != null) {
            for (String arg : args) {
                addMessageIfPresent(messages, arg);
            }
        }
        addMessageIfPresent(messages, serverException.getMessage());
    }

    private void addMessageIfPresent(LinkedHashSet<String> messages, String rawMessage) {
        if (rawMessage != null && !rawMessage.isBlank()) {
            messages.add(rawMessage.trim());
        }
    }

    private record FieldBinding(
            String fieldName,
            Class<?> fieldType,
            String columnName,
            int columnIndex,
            boolean required
    ) {
    }

    private record BindingResult<T>(T dto, List<String> errors) {
    }

    private record ValidRow<T>(long rowNumber, T dto) {
    }

    private record PersistSummary(int successCount, int warningCount) {
    }
}
