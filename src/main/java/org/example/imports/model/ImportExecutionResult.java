package org.example.imports.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public record ImportExecutionResult(
        int totalCount,
        int successCount,
        int failedCount,
        String message,
        List<ImportRowError> rowErrors,
        byte[] errorFileContent,
        String errorFileName,
        String errorFileContentType
) {

    public static ImportExecutionResult empty() {
        return new ImportExecutionResult(
                0, 0, 0,
                "Successfully imported 0 records.",
                Collections.emptyList(),
                null, null, null
        );
    }

    public static ImportExecutionResult success(int totalCount, int successCount) {
        return new ImportExecutionResult(
                totalCount,
                successCount, 0,
                "Successfully imported %d records.".formatted(successCount),
                Collections.emptyList(),
                null, null, null
        );
    }

    public static ImportExecutionResult withErrors(
            int totalCount,
            int successCount,
            int failedCount,
            List<ImportRowError> rowErrors,
            byte[] errorFileContent,
            String errorFileName,
            String errorFileContentType
    ) {
        String message = "Successfully imported %d records. %d records failed, please check the downloaded file."
                .formatted(successCount, failedCount);
        return new ImportExecutionResult(
                totalCount,
                successCount,
                failedCount,
                message,
                rowErrors == null ? Collections.emptyList() : List.copyOf(rowErrors),
                errorFileContent,
                errorFileName,
                errorFileContentType
        );
    }

    public boolean hasFailures() {
        return failedCount > 0;
    }

    public boolean hasErrorFile() {
        return errorFileContent != null && errorFileContent.length > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ImportExecutionResult other)) return false;
        return totalCount == other.totalCount
                && successCount == other.successCount
                && failedCount == other.failedCount
                && Arrays.equals(errorFileContent, other.errorFileContent)
                && Objects.equals(message, other.message)
                && Objects.equals(rowErrors, other.rowErrors)
                && Objects.equals(errorFileName, other.errorFileName)
                && Objects.equals(errorFileContentType, other.errorFileContentType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(totalCount, successCount, failedCount, message, rowErrors, errorFileName, errorFileContentType);
        result = 31 * result + Arrays.hashCode(errorFileContent);
        return result;
    }

    @Override
    public String toString() {
        return "ImportExecutionResult[" +
                "totalCount=" + totalCount +
                ", " +
                "successCount=" + successCount +
                ", failedCount=" + failedCount +
                ", message=" + message +
                ", rowErrors.size=" + (rowErrors != null ? rowErrors.size() : "null") +
                ", errorFileContent.length=" + (errorFileContent != null ? errorFileContent.length : "null") +
                ", errorFileName=" + errorFileName +
                ", errorFileContentType=" + errorFileContentType +
                ']';
    }
}
