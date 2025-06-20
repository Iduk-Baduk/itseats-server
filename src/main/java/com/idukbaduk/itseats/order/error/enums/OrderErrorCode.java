package com.idukbaduk.itseats.order.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    MENU_OPTION_SERIALIZATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "menuOption 직렬화 실패"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 조회 실패");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[ORDER ERROR] " + message;
    }
}
