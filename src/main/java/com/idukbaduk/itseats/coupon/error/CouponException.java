package com.idukbaduk.itseats.coupon.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class CouponException extends BaseException {

    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }
}
