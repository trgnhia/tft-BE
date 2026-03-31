package org.example.common.exception;

import lombok.Getter;
import org.example.common.enums.ErrorCode;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class ServerException extends RuntimeException {
    private final ErrorCode errorCode;
    private final HttpStatus status;
    private final String[] args;
    private final transient List<Object> data;

    public ServerException(ErrorCode errorCode){
        super(errorCode.name());
        this.errorCode = errorCode;
        this.status = HttpStatus.BAD_REQUEST;
        this.args = new String[0];
        this.data = null;
    }

    public ServerException(String message, ErrorCode errorCode, String[] args, List<Object> data){
        super(message);
        this.errorCode = errorCode;
        this.args = args;
        this.data = data;
        this.status = HttpStatus.BAD_REQUEST;
    }

    public ServerException(String message, ErrorCode errorCode, HttpStatus status, String[] args, List<Object> data) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
        this.args = args;
        this.data = data;
    }

    public ServerException(String message, ErrorCode errorCode, HttpStatus status, List<Object> data, String... args) {
        super(message);
        this.errorCode = errorCode;
        this.data = data;
        this.args = args;
        this.status = status;
    }


    public ServerException(ErrorCode errorCode, HttpStatus status, String[] params, List<Object> data) {
        super(params != null && params.length > 0 ? params[0] : errorCode.name());
        this.errorCode = errorCode;
        this.status = status;
        this.args = params;
        this.data = data;
    }
}
