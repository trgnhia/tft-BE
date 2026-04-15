package org.example.imports.service.impl;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.exception.ServerException;
import org.example.annotations.ImportColumn;
import org.example.imports.model.ImportExecutionResult;
import org.example.imports.model.ImportRow;
import org.example.imports.model.ParsedImportFile;
import org.example.imports.service.GenericImportService;
import org.example.imports.service.ImportRowPersister;
import org.example.imports.strategy.ImportFileStrategy;
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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.example.util.MessageUtils.getMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class GenericImportServiceImpl implements GenericImportService {

    private static final int MAX_ROWS = 50_000;
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
        List<ValidRow<T>> validRows = collectValidRows(dtoClass, fieldBindings, parsedFile.rows(), rowErrors);
        int successCount = persistValidRows(rowPersister, validRows, rowErrors);
        int failedCount = rowErrors.size();

        log.info("Import completed: file={}, success={}, failed={}", file.getOriginalFilename(), successCount, failedCount);

        if (failedCount == 0) {
            return ImportExecutionResult.success(successCount);
        }

        Map<Long, String> errorDetailsByRow = joinErrorsByRow(rowErrors);

        byte[] errorFileContent = writeErrorFile(strategy, parsedFile, errorDetailsByRow);
        String errorFileName = buildErrorFileName(file.getOriginalFilename(), strategy.outputFileExtension());

        return ImportExecutionResult.withErrors(
                successCount, failedCount,
                errorFileContent, errorFileName, strategy.outputContentType()
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
            Map<Long, List<String>> rowErrors
    ) {
        List<ValidRow<T>> validRows = new ArrayList<>();
        for (ImportRow row : rows) {
            BindingResult<T> result = bindAndValidateRow(dtoClass, fieldBindings, row);
            if (result.errors().isEmpty()) {
                validRows.add(new ValidRow<>(row.rowNumber(), result.dto()));
            } else {
                log.debug("Row {} failed validation: {}", row.rowNumber(), result.errors());
                addRowErrors(rowErrors, row.rowNumber(), result.errors());
            }
        }
        return validRows;
    }

    private <T> int persistValidRows(
            ImportRowPersister<T> rowPersister,
            List<ValidRow<T>> validRows,
            Map<Long, List<String>> rowErrors
    ) {
        int successCount = 0;
        for (ValidRow<T> validRow : validRows) {
            try {
                rowPersister.persist(validRow.dto());
                successCount++;
            } catch (RuntimeException ex) {
                log.warn("Row {} failed to persist", validRow.rowNumber(), ex);
                addRowErrors(rowErrors, validRow.rowNumber(), extractExceptionMessages(ex));
            }
        }
        return successCount;
    }

    private Map<Long, String> joinErrorsByRow(Map<Long, List<String>> rowErrors) {
        return rowErrors.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.join("; ", entry.getValue()),
                        (left, right) -> left,
                        LinkedHashMap::new
                ));
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

    private byte[] writeErrorFile(
            ImportFileStrategy strategy,
            ParsedImportFile parsedFile,
            Map<Long, String> errorsByRow
    ) {
        try {
            return strategy.buildErrorFile(parsedFile, errorsByRow);
        } catch (IOException ex) {
            throw new IllegalStateException(getMessage(Constants.MessageKey.IMPORT_BUILD_ERROR_FILE_FAILED), ex);
        }
    }

    private <T> List<FieldBinding> resolveFieldBindings(Class<T> dtoClass, List<String> headers) {
        Map<String, Integer> headerIndexMap = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            headerIndexMap.put(normalize(headers.get(i)), i);
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
                    fieldBindings.add(
                            new FieldBinding(
                                    field.getName(),
                                    field.getType(),
                                    annotation.name(),
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

    private String buildErrorFileName(String originalFileName, String extension) {
        String safeName = (originalFileName == null || originalFileName.isBlank())
                ? "import-file"
                : originalFileName;
        int dotIndex = safeName.lastIndexOf('.');
        String baseName = dotIndex > 0 ? safeName.substring(0, dotIndex) : safeName;
        return baseName + "-errors." + extension;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
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
}
