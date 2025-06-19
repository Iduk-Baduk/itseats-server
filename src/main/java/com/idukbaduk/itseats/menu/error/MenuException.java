package com.idukbaduk.itseats.menu.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class MenuException extends BaseException {

    public MenuException(ErrorCode errorCode) {
        super(errorCode);
    }
}

