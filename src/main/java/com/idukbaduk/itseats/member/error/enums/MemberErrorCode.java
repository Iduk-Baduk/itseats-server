package com.idukbaduk.itseats.member.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다."),
    MEMBER_USERNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 아이디입니다."),
    MEMBER_NICKNAME_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 닉네임입니다."),
    MEMBER_EMAIL_DUPLICATED(HttpStatus.BAD_REQUEST, "중복된 이메일입니다."),
    INVALID_USERNAME(HttpStatus.NOT_FOUND, "유효하지 않은 사용자 이름입니다."),
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.");

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
