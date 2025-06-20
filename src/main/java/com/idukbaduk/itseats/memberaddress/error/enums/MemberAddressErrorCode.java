package com.idukbaduk.itseats.memberaddress.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MemberAddressErrorCode implements ErrorCode {

    MEMBER_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "회원 주소 조회 실패");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[MEMBER ADDRESS ERROR] " + message;
    }
}
