package com.idukbaduk.itseats.order.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class OrderException extends BaseException {

    public OrderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
