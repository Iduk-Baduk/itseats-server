package com.idukbaduk.itseats.memberaddress.dto.enums;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AddressResponse implements Response {

    ADDRESS_CREATE_SUCCESS(HttpStatus.CREATED, "주소 추가 성공");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
