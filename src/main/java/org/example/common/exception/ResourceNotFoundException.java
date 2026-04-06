package org.example.common.exception;

import org.example.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ServerException {
    public ResourceNotFoundException(String... parameters) {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, parameters, null);
    }
}
