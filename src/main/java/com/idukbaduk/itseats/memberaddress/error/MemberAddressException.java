package com.idukbaduk.itseats.memberaddress.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class MemberAddressException extends BaseException {

    public MemberAddressException(ErrorCode errorCode) {
        super(errorCode);
    }
}
