package com.idukbaduk.itseats.coupon.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "쿠폰을 찾을 수 없습니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "쿠폰의 유효 기간이 만료되었습니다."),
    INSUFFICIENT_ORDER_AMOUNT(HttpStatus.BAD_REQUEST, "쿠폰을 사용하기 위한 최소 주문 금액이 충족되지 않았습니다."),
    ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),
    QUANTITY_EXCEEDED(HttpStatus.CONFLICT, "쿠폰 발급 수량을 초과했습니다."),
    INVALID_PERIOD(HttpStatus.BAD_REQUEST, "쿠폰 발급 가능 기간이 아닙니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.CONFLICT, "쿠폰 발급 처리 중입니다. 잠시 후 다시 시도해주세요."),
    LOCK_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "쿠폰 발급 처리 중 오류가 발생했습니다."),
    INVALID_DATE_RANGE(HttpStatus.BAD_REQUEST, "날짜 입력이 올바르지 않습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[COUPON ERROR] " + message;
    }
}
