package com.idukbaduk.itseats.payment.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentResponse implements Response {

    CREATE_PAYMENT_SUCCESS(HttpStatus.CREATED, "결제 생성 성공"),
    CONFIRM_PAYMENT_SUCCESS(HttpStatus.OK, "결제 승인 성공"),
    TOSS_PAYMENT_CONFIRM_SUCCESS(HttpStatus.OK, "토스페이먼츠 결제 승인 성공");

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
