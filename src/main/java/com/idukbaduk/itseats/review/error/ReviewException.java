package com.idukbaduk.itseats.review.error;

import com.idukbaduk.itseats.global.error.core.BaseException;
import com.idukbaduk.itseats.global.error.core.ErrorCode;

public class ReviewException extends BaseException {

    public ReviewException(ErrorCode errorCode) {
        super(errorCode);
    }
}
