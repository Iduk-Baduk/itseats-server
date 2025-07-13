package com.idukbaduk.itseats.payment.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class PaymentException extends BaseException {
    private final String originalErrorBody;

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
        this.originalErrorBody = null;
    }

    public PaymentException(ErrorCode errorCode, String message, String originalErrorBody) {
        super(errorCode, message);
        this.originalErrorBody = originalErrorBody;
    }

    public String getOriginalErrorBody() {
        return originalErrorBody;
    }
}
