package com.idukbaduk.itseats.coupon.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum CouponResponse implements Response {

    CREATE_COUPON_SUCCESS(HttpStatus.CREATED, "쿠폰을 생성했습니다.")
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
