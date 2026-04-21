package org.example.common.exception.handler;

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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.unit.DataSize;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String ERROR_LOG_PREFIX = "error.";
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private DataSize maxFileSize;

    @Value("${spring.servlet.multipart.max-request-size:10MB}")
    private DataSize maxRequestSize;

    @ExceptionHandler(ServerException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Void>> handleServerException(ServerException ex) {
        log.error("ServerException occurred: {}", ex.getMessage(), ex);
        String[] args = ex.getArgs() == null ? new String[0] : ex.getArgs();
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                (Object[]) args);
        return new ResponseEntity<>(
                ApiResponse.error(msg, ex.getErrorCode().getCode(), ex.getMessage()), ex.getStatus()
        );
    }

    @Override
    @Nullable
    @SuppressWarnings("java:S2638")
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders httpHeaders,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        List<ParamError> errors = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new ParamError(error.getField(), error.getDefaultMessage()))
                .toList();
        return ResponseEntity.badRequest().body(
                ApiResponse.error(errors, ErrorCode.INVALID_PARAMETER.getCode()));
    }

    @ExceptionHandler(DataException.class)
    public ResponseEntity<ApiResponse<Void>> handleDataException(DataException ex) {
        log.error("DataException Error ", ex);
        String[] args = ex.getArgs() == null ? new String[0] : ex.getArgs();
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                (Object[]) args);
        return new ResponseEntity<>(
                ApiResponse.error(msg, ErrorCode.UNEXPECTED_ERROR.getCode(), ex.getMessage()), ex.getStatus()
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(ConflictException ex) {
        log.error("ConflictException Error ", ex);

        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                (Object[]) ex.getArgs()
        );

        return new ResponseEntity<>(
                ApiResponse.error(msg, ex.getErrorCode().getCode(), ex.getMessage()),
                ex.getStatus()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException Error ", ex);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth instanceof AnonymousAuthenticationToken) {
            // Treat as 401 Unauthorized if user is not authenticated
            ErrorCode unauthorize = ErrorCode.UNAUTHORIZED;
            String msg = MessageUtils.getMessage(ERROR_LOG_PREFIX + unauthorize.name(), null, "");
            return new ResponseEntity<>(
                    ApiResponse.error(msg, unauthorize.getCode()), HttpStatus.UNAUTHORIZED
            );
        }

        ErrorCode forbiddenCode = ErrorCode.PERMISSION_DENIED;
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + forbiddenCode.name(),
                null,
                "");
        return new ResponseEntity<>(
                ApiResponse.error(msg, forbiddenCode.getCode()), HttpStatus.FORBIDDEN
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication Error ", ex);
        ErrorCode unauthorize = ErrorCode.UNAUTHORIZED;
        String msg = MessageUtils.getMessage(ERROR_LOG_PREFIX + unauthorize.name(), null, "");
        return new ResponseEntity<>(
                ApiResponse.error(msg, unauthorize.getCode()), HttpStatus.UNAUTHORIZED
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> {
                            String path = violation.getPropertyPath().toString();
                            return path.substring(path.lastIndexOf('.') + 1);
                        },
                        ConstraintViolation::getMessage,
                        (existing, replacement) -> existing
                ));
        return ResponseEntity.badRequest().body(
                ApiResponse.error(errors, ErrorCode.INVALID_PARAMETER.getCode()));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ApiResponse<Void>> handleMultipartException(MultipartException ex) {
        log.warn("Multipart request error", ex);
        boolean fileSizeExceeded = ex instanceof MaxUploadSizeExceededException;
        boolean requestSizeExceeded = !fileSizeExceeded && isLikelyRequestSizeExceeded(ex);
        String messageKey;
        Object argument;
        if (fileSizeExceeded) {
            messageKey = Constants.MessageKey.IMPORT_FILE_SIZE_EXCEEDED;
            argument = maxFileSize;
        } else if (requestSizeExceeded) {
            messageKey = Constants.MessageKey.IMPORT_REQUEST_SIZE_EXCEEDED;
            argument = maxRequestSize;
        } else {
            messageKey = Constants.MessageKey.IMPORT_MULTIPART_INVALID;
            argument = "";
        }
        String msg = MessageUtils.getMessage(messageKey, argument);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(msg, ErrorCode.INVALID_PARAMETER.getCode()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage(), ErrorCode.INVALID_PARAMETER.getCode()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnwantedException(Exception ex) {
        log.error("Error ", ex);
        ErrorCode unexpectedCode = ErrorCode.UNEXPECTED_ERROR;
        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + unexpectedCode.getCode(),
                null,
                "");
        return new ResponseEntity<>(
                ApiResponse.error(msg, unexpectedCode.getCode()), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        log.warn("Resource not found: {}", ex.getMessage());

        String combinedArgs = String.join(" ", ex.getArgs());

        String msg = MessageUtils.getMessage(
                ERROR_LOG_PREFIX + ex.getErrorCode(),
                combinedArgs
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(msg, ErrorCode.NOT_FOUND.getCode()));
    }

    private boolean isLikelyRequestSizeExceeded(MultipartException ex) {
        if (ex instanceof MaxUploadSizeExceededException) {
            return true;
        }
        String msg = ex.getMessage();
        return msg != null && msg.toLowerCase().contains("size");
    }
}
