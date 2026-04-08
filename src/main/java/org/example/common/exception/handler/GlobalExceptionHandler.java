package org.example.common.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import lombok.extern.slf4j.Slf4j;
import org.example.common.constant.Constants;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.common.exception.ServerException;
import org.example.common.exception.base.ParamError;
import org.example.core.api.ApiResponse;
import org.example.util.MessageUtils;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
// LƯU Ý: Sửa lại import AccessDeniedException của Spring Security
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_LOG_PREFIX = "error.";

    @ExceptionHandler(ServerException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> handleServerException(ServerException ex, HttpServletRequest request) { // Thêm request
        log.error("ServerException occurred: {}", ex.getMessage(), ex);
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                ex.getArgs(),
                "");

        request.setAttribute("errorMessage", msg);

        return new ResponseEntity<>(
                ApiResponse.error(msg, ex.getErrorCode().name(), ex.getMessage()), ex.getStatus()
        );
    }

    @Override
    @Nullable
    @SuppressWarnings("java:S2638")
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders httpHeaders,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) { // Dùng WebRequest
        List<ParamError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ParamError(error.getField(), error.getDefaultMessage()))
                .toList();

        // Gộp tất cả các lỗi nhập liệu thành 1 chuỗi để lưu Log DB
        String msg = errors.stream()
                .map(e -> e.getField() + ": " + e.getMessage())
                .collect(Collectors.joining("; "));

        request.setAttribute("errorMessage", msg, WebRequest.SCOPE_REQUEST); // Nhét lỗi vào WebRequest

        return ResponseEntity.badRequest().body(
                ApiResponse.error(errors,ErrorCode.INVALID_PARAMETER.getCode()));
    }

    @ExceptionHandler(DataException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataException(DataException ex, HttpServletRequest request) { // Thêm request
        log.error("DataException Error ", ex);
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                ex.getArgs(),
                "");

        request.setAttribute("errorMessage", msg);

        return new ResponseEntity<>(
                ApiResponse.error(msg, ErrorCode.UNEXPECTED_ERROR.getCode(), ex.getMessage()), ex.getStatus()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(ConflictException ex, HttpServletRequest request) { // Thêm request
        log.error("ConflictException Error ", ex);
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                ex.getArgs(),
                "");

        request.setAttribute("errorMessage", msg);

        return new ResponseEntity<>(
                ApiResponse.error(msg, ex.getErrorCode().name(), ex.getMessage()), ex.getStatus()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) { // Thêm request
        log.error("AccessDeniedException Error ", ex);
        ErrorCode forbiddenCode = ErrorCode.PERMISSION_DENIED;
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + forbiddenCode.getCode(),
                null,
                "");

        request.setAttribute("errorMessage", msg);

        return new ResponseEntity<>(
                ApiResponse.error(msg, forbiddenCode.name()), HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));

        // Sửa lỗi ở đây: Tạo biến msg bằng cách gộp các lỗi lại
        String msg = errors.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));

        request.setAttribute("errorMessage", msg);

        return ResponseEntity.badRequest().body(
                ApiResponse.error(errors,ErrorCode.INVALID_PARAMETER.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnwantedException(Exception ex, HttpServletRequest request) {
        log.error("Error ", ex);
        ErrorCode unexpectedCode = ErrorCode.UNEXPECTED_ERROR;
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + unexpectedCode.getCode(),
                null,
                "");


        request.setAttribute("errorMessage", ex.getMessage() != null ? ex.getMessage() : msg);

        return new ResponseEntity<>(
                ApiResponse.error(msg, unexpectedCode.name()), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());

        String msg = MessageUtils.getMessage(
                Constants.MessageKey.ERROR_NOT_FOUND,
                ex.getMessage()
        );
        request.setAttribute("errorMessage", msg);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(msg, ErrorCode.NOT_FOUND.name()));
    }
}