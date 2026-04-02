package org.example.common.enums;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // Error data: 1xxx
    NOT_FOUND("1000"),
    ALREADY_EXISTS("1001"),
    INCOMPLETE_DATA("1002"),
    FORMAT_INCORRECT("1003"),

    // Error access: 2xxx
    PERMISSION_DENIED("2000"),
    // Error user: 3xxx
    INVALID_PARAMETER("3000"),
    PASSWORD_INCORRECT("3001"),
    MIN_LENGTH("3002"),
    MAX_LENGTH("3003"),
    FAIL_ATTEMPT("3004"),
    // Error system: 4xxx
    UNEXPECTED_ERROR("4000"),
    SERVICE_UNAVAILABLE("4001"),
    CONNECTION("4002"),


    // champ
    CHAMP_NOT_FOUND("5001"),
    CHAMP_NOT_DELETED("5005"),

    //set
    SET_NOT_FOUND("6001");
    ErrorCode(String code) {
        this.code = code;
    }

    private final String code;
}
