package com.idukbaduk.itseats.store.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class StoreException extends BaseException {

    public StoreException(ErrorCode errorCode) {
        super(errorCode);
    }
}
