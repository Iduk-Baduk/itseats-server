package com.idukbaduk.itseats.payment.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "결제 조회 실패"),
    TOSS_PAYMENT_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "토스 결제 서버 에러"),
    PAYMENT_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "요청 금액과 실제 금액이 다릅니다."),
    PAYMENT_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "결제 실패. 다시 시도해주세요.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[PAYMENT ERROR] " + message;
    }
}
