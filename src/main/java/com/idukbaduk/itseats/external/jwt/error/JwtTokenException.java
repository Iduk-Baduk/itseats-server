package com.idukbaduk.itseats.external.jwt.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class JwtTokenException extends BaseException  {
    public JwtTokenException(ErrorCode errorCode) {
        super(errorCode);
    }
}
