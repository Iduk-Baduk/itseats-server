package com.idukbaduk.itseats.payment.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class PaymentException extends BaseException {

    public PaymentException(ErrorCode errorCode) {
        super(errorCode);
    }
}
