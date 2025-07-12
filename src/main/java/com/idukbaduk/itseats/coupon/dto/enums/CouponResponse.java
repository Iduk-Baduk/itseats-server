package com.idukbaduk.itseats.coupon.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CouponResponse implements Response {

    GET_MY_COUPONS(HttpStatus.OK, "내 쿠폰 조회 성공"),
    ISSUE_COUPON_SUCCESS(HttpStatus.CREATED, "쿠폰이 발급되었습니다."),
    CREATE_COUPON_SUCCESS(HttpStatus.CREATED, "쿠폰을 생성했습니다."),
    GET_ALL_COUPONS(HttpStatus.OK, "모든 쿠폰 조회 성공"),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
