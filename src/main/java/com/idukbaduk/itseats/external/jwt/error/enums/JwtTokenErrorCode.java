package com.idukbaduk.itseats.external.jwt.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum JwtTokenErrorCode implements ErrorCode {

    TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "만료된 토큰"),
    TOKEN_UNTRUSTWORTHY(HttpStatus.FORBIDDEN, "신뢰할 수 없는 토큰"),
    TOKEN_ISSUE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 발행 실패");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[TOKEN ERROR] " + message;
    }

}
