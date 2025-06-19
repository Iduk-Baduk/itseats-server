package com.idukbaduk.itseats.member.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class MemberException extends BaseException {

    public MemberException(ErrorCode errorCode) {
        super(errorCode);
    }
}
