package com.idukbaduk.itseats.auths.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    AUTHORIZATION_PARSE_FAILED(HttpStatus.BAD_REQUEST, "로그인 정보를 올바르게 입력해주세요."),
    AUTHORIZATION_USERNAME_EMPTY(HttpStatus.BAD_REQUEST, "아이디를 입력해주세요."),
    AUTHORIZATION_PASSWORD_EMPTY(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요.");
    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[MEMBER ERROR] " + message;
    }
}
