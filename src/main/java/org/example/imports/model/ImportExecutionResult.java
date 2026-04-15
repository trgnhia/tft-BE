package org.example.imports.model;

import java.util.Arrays;
import java.util.Objects;

public record ImportExecutionResult(
        int successCount,
        int failedCount,
        String message,
        byte[] errorFileContent,
        String errorFileName,
        String errorFileContentType
) {

    public static ImportExecutionResult empty() {
        return new ImportExecutionResult(0, 0, "Successfully imported 0 records.", null, null, null);
    }

    public static ImportExecutionResult success(int successCount) {
        return new ImportExecutionResult(
                successCount, 0,
                "Successfully imported %d records.".formatted(successCount),
                null, null, null
        );
    }

    public static ImportExecutionResult withErrors(
            int successCount,
            int failedCount,
            byte[] errorFileContent,
            String errorFileName,
            String errorFileContentType
    ) {
        String message = "Successfully imported %d records. %d records failed, please check the downloaded file."
                .formatted(successCount, failedCount);
        return new ImportExecutionResult(successCount, failedCount, message, errorFileContent, errorFileName, errorFileContentType);
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
        return successCount == other.successCount
                && failedCount == other.failedCount
                && Arrays.equals(errorFileContent, other.errorFileContent)
                && Objects.equals(message, other.message)
                && Objects.equals(errorFileName, other.errorFileName)
                && Objects.equals(errorFileContentType, other.errorFileContentType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(successCount, failedCount, message, errorFileName, errorFileContentType);
        result = 31 * result + Arrays.hashCode(errorFileContent);
        return result;
    }

    @Override
    public String toString() {
        return "ImportExecutionResult[" +
                "successCount=" + successCount +
                ", failedCount=" + failedCount +
                ", message=" + message +
                ", errorFileContent.length=" + (errorFileContent != null ? errorFileContent.length : "null") +
                ", errorFileName=" + errorFileName +
                ", errorFileContentType=" + errorFileContentType +
                ']';
    }
}