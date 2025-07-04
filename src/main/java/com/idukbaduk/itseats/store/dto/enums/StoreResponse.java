package com.idukbaduk.itseats.store.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StoreResponse implements Response {

    CREATE_STORE_SUCCESS(HttpStatus.CREATED, "가게 추가 성공"),
    UPDATE_STATUS_SUCCESS(HttpStatus.OK, "가게 상태 변경 성공"),
    PAUSE_ORDER_SUCCESS(HttpStatus.OK, "주문 일시정지 성공"),
    GET_STORES_SUCCESS(HttpStatus.OK, "전체 가게 목록 조회 성공"),
    GET_STORES_BY_CATEGORY_SUCCESS(HttpStatus.OK, "카테고리 별 가게 목록 조회 성공"),
    GET_STORE_DETAIL_SUCCESS(HttpStatus.OK, "가게 상세 조회 성공"),
    ;

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
