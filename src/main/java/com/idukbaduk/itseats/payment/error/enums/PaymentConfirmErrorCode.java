package com.idukbaduk.itseats.payment.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentConfirmErrorCode implements ErrorCode {
    ;

    private HttpStatus httpStatus;
    private String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[PAYMENT CONFIRM ERROR] " + message;
    }
}
