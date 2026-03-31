package org.example.common.exception;

import org.example.common.enums.ErrorCode;


public class ConflictException extends DataException{
    public ConflictException() {
        super(ErrorCode.ALREADY_EXISTS);
    }

    public ConflictException(String... params) {
        super(ErrorCode.ALREADY_EXISTS, params);
    }
}
