package com.idukbaduk.itseats.rider.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RiderErrorCode implements ErrorCode {

    RIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "라이더 조회 실패");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[RIDER ERROR] " + message;
    }
}
