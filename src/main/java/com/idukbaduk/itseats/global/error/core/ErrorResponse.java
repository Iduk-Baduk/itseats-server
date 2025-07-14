package com.idukbaduk.itseats.global.error.core;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String message;
    private final String originalErrorBody; // 추가

    public static ErrorResponse of(ErrorCode errorCode) {
        return new ErrorResponse(errorCode.getStatus().value(), errorCode.getMessage(), null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode.getStatus().value(), message, null);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String originalErrorBody) {
        return new ErrorResponse(errorCode.getStatus().value(), message, originalErrorBody);
    }
}
