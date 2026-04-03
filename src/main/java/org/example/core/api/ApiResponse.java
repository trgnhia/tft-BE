package org.example.core.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.example.common.constant.Constants;
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private Object message;
    private T data;
    private String code;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String detail;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(Constants.Api.SUCCESS_MESSAGE)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(Object message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(errorCode)
                .build();
    }

    public static <T> ApiResponse<T> error(Object message, String errorCode, String detail) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .code(errorCode)
                .detail(detail)
                .build();
    }


}
