package com.idukbaduk.itseats.memberaddress.dto.enums;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum AddressResponse implements Response {

    CREATE_ADDRESS_SUCCESS(HttpStatus.CREATED, "주소 추가 성공"),
    GET_ADDRESS_LIST_SUCCESS(HttpStatus.OK, "주소 목록 조회 성공"),
    UPDATE_ADDRESS_SUCCESS(HttpStatus.OK, "주소 수정 성공"),
    DELETE_ADDRESS_SUCCESS(HttpStatus.NO_CONTENT, "주소 삭제 성공");

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
