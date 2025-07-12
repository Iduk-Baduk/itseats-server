package com.idukbaduk.itseats.payment.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentConfirmErrorCode implements ErrorCode {
    
    INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "유효하지 않은 결제 키입니다."),
    INVALID_ORDER_ID(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 ID입니다."),
    PAYMENT_ALREADY_CONFIRMED(HttpStatus.BAD_REQUEST, "이미 승인된 결제입니다."),
    PAYMENT_CANCELED(HttpStatus.BAD_REQUEST, "취소된 결제입니다."),
    INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "잔액이 부족합니다."),
    CARD_DECLINED(HttpStatus.BAD_REQUEST, "카드 결제가 거부되었습니다."),
    NETWORK_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네트워크 오류가 발생했습니다."),
    UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "알 수 없는 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[PAYMENT CONFIRM ERROR] " + message;
    }
}
