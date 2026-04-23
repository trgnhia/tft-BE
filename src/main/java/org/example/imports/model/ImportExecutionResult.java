package org.example.imports.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.example.util.MessageUtils.getMessage;

public record ImportExecutionResult(
        int totalCount,
        int successCount,
        int failedCount,
        int warningCount,
        String message,
        List<ImportRowError> rowErrors,
        List<ImportRowReport> rowReports,
        byte[] errorFileContent,
        String errorFileName,
        String errorFileContentType
) {

    public static ImportExecutionResult empty() {
        return new ImportExecutionResult(
                0, 0, 0, 0,
                getMessage("import.execution.empty"),
                Collections.emptyList(),
                Collections.emptyList(),
                null, null, null
        );
    }

    public static ImportExecutionResult success(int totalCount, int successCount) {
        return success(totalCount, successCount, Collections.emptyList());
    }

    public static ImportExecutionResult success(
            int totalCount,
            int successCount,
            List<ImportRowReport> rowReports
    ) {
        return new ImportExecutionResult(
                totalCount,
                successCount,
                0,
                0,
                getMessage("import.execution.success", successCount),
                Collections.emptyList(),
                rowReports == null ? Collections.emptyList() : List.copyOf(rowReports),
                null, null, null
        );
    }

    public static ImportExecutionResult withIssues(
            int totalCount,
            int successCount,
            int failedCount,
            int warningCount,
            List<ImportRowError> rowErrors,
            List<ImportRowReport> rowReports,
            byte[] errorFileContent,
            String errorFileName,
            String errorFileContentType
    ) {
        String message;
        if (failedCount > 0 && warningCount > 0) {
            message = getMessage(
                    "import.execution.with_issues.warning_and_error",
                    successCount,
                    warningCount,
                    failedCount
            );
        } else if (failedCount > 0) {
            message = getMessage("import.execution.with_issues.error", successCount, failedCount);
        } else {
            message = getMessage("import.execution.with_issues.warning", successCount, warningCount);
        }
        return new ImportExecutionResult(
                totalCount,
                successCount,
                failedCount,
                warningCount,
                message,
                rowErrors == null ? Collections.emptyList() : List.copyOf(rowErrors),
                rowReports == null ? Collections.emptyList() : List.copyOf(rowReports),
                errorFileContent,
                errorFileName,
                errorFileContentType
        );
    }

    public boolean hasFailures() {
        return failedCount > 0;
    }

    public boolean hasWarnings() {
        return warningCount > 0;
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
                && warningCount == other.warningCount
                && Arrays.equals(errorFileContent, other.errorFileContent)
                && Objects.equals(message, other.message)
                && Objects.equals(rowErrors, other.rowErrors)
                && Objects.equals(rowReports, other.rowReports)
                && Objects.equals(errorFileName, other.errorFileName)
                && Objects.equals(errorFileContentType, other.errorFileContentType);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(
                totalCount,
                successCount,
                failedCount,
                warningCount,
                message,
                rowErrors,
                rowReports,
                errorFileName,
                errorFileContentType
        );
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
                ", warningCount=" + warningCount +
                ", message=" + message +
                ", rowErrors.size=" + (rowErrors != null ? rowErrors.size() : "null") +
                ", rowReports.size=" + (rowReports != null ? rowReports.size() : "null") +
                ", errorFileContent.length=" + (errorFileContent != null ? errorFileContent.length : "null") +
                ", errorFileName=" + errorFileName +
                ", errorFileContentType=" + errorFileContentType +
                ']';
    }
}
