package com.idukbaduk.itseats.rider.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class RiderException extends BaseException {

    public RiderException(ErrorCode errorCode) {
        super(errorCode);
    }
}
