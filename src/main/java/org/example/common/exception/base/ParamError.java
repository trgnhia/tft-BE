package org.example.common.exception.base;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParamError {
    private String field;
    private String message;

    public ParamError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
