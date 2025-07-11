package com.idukbaduk.itseats.external.jwt.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum JwtTokenErrorCode implements ErrorCode {

    TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "만료된 토큰입니다."),
    TOKEN_UNTRUSTWORTHY(HttpStatus.FORBIDDEN, "신뢰할 수 없는 토큰입니다."),
    TOKEN_ISSUE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 발급을 실패했습니다."),
    TOKEN_SECRET_KEY_INVALID_LENGTH(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 시크릿 키의 길이가 유효하지 않습니다.");

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
