package org.example.common.exception;

import org.example.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

public class DataException extends ServerException{
    public DataException(ErrorCode errorCode, String... params) {
        super(errorCode, HttpStatus.BAD_REQUEST, params, null);
    }

    public DataException(ErrorCode errorCode, List<Object> data, String... params) {
        super(errorCode, HttpStatus.BAD_REQUEST, params, data);
    }

    public DataException(ErrorCode errorCode, Object[] params) {
        super(errorCode,
                HttpStatus.BAD_REQUEST,
                java.util.Arrays.stream(params).map(String::valueOf).toArray(String[]::new),
                null);
    }
}
