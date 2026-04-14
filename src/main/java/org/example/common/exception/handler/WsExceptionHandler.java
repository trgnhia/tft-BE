package org.example.common.exception.handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.common.enums.ErrorCode;
import org.example.common.exception.ConflictException;
import org.example.common.exception.DataException;
import org.example.common.exception.ResourceNotFoundException;
import org.example.common.exception.base.ParamError;
import org.example.core.api.ApiResponse;
import org.example.util.MessageUtils;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.security.Principal;
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WsExceptionHandler {

    private static final String ERROR_DESTINATION = "/queue/errors";
    private static final String ERROR_PREFIX = "error.";

    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    public void handleValidationException(MethodArgumentNotValidException ex,
                                          Principal principal) {

        List<ParamError> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapToParamError)
                .toList();

        log.warn("Validation error (WS): {}", errors);

        sendError(
                principal,
                ApiResponse.error(errors, ErrorCode.INVALID_PARAMETER.getCode())
        );
    }

    @MessageExceptionHandler(DataException.class)
    public void handleDataException(DataException ex, Principal principal) {

        log.error("DataException (WS): {}", ex.getMessage(), ex);

        String msg = MessageUtils.getMessage(
                ERROR_PREFIX + ex.getErrorCode(),
                ex.getArgs(),
                ""
        );

        sendError(
                principal,
                ApiResponse.error(msg, ex.getErrorCode().getCode(), ex.getMessage())
        );
    }

    @MessageExceptionHandler(ConflictException.class)
    public void handleConflictException(ConflictException ex, Principal principal) {

        log.error("ConflictException (WS): {}", ex.getMessage(), ex);

        String msg = MessageUtils.getMessage(
                ERROR_PREFIX + ex.getErrorCode(),
                (Object[]) ex.getArgs()
        );

        sendError(
                principal,
                ApiResponse.error(msg, ex.getErrorCode().getCode(), ex.getMessage())
        );
    }

    @MessageExceptionHandler(ResourceNotFoundException.class)
    public void handleNotFoundException(ResourceNotFoundException ex, Principal principal) {

        log.warn("Resource not found (WS): {}", ex.getMessage());

        String combinedArgs = String.join(" ", ex.getArgs());

        String msg = MessageUtils.getMessage(
                ERROR_PREFIX + ex.getErrorCode(),
                combinedArgs
        );

        sendError(
                principal,
                ApiResponse.error(msg, ErrorCode.NOT_FOUND.getCode())
        );
    }

    private void sendError(Principal principal, ApiResponse<?> response) {
        if (principal == null) {
            log.warn("Principal is null, cannot send WS error");
            return;
        }
        simpMessagingTemplate.convertAndSendToUser(
                principal.getName(),
                ERROR_DESTINATION,
                response
        );
    }

    private ParamError mapToParamError(FieldError error) {
        return new ParamError(
                error.getField(),
                error.getDefaultMessage()
        );
    }
}