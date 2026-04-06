package org.example.common.exception;

import org.example.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

public class ResourceNotFoundException extends ServerException {

    public ResourceNotFoundException(String... params) {
        super(ErrorCode.NOT_FOUND, HttpStatus.NOT_FOUND, params, null);
    }

    public ResourceNotFoundException(String messageKey, Object[] objects) {
        super(messageKey,
                ErrorCode.NOT_FOUND,
                HttpStatus.NOT_FOUND,
                null,
                Arrays.stream(objects).map(String::valueOf).toArray(String[]::new)
        );
    }
}