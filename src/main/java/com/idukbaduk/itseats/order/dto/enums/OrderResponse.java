package com.idukbaduk.itseats.order.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrderResponse implements Response {

    GET_ORDERS_SUCCESS(HttpStatus.OK, "과거 주문 내역 조회 성공"),
    GET_ORDER_DETAILS_SUCCESS(HttpStatus.OK, "주문 정보 상세 조회 성공"),
    GET_ORDER_STATUS_SUCCESS(HttpStatus.OK, "주문 현황 조회 성공"),
    GET_STORE_ORDERS_SUCCESS(HttpStatus.OK, "주문 접수 조회 성공"),
    REJECT_ORDER_SUCCESS(HttpStatus.OK, "주문 거절 완료"),
    ACCEPT_ORDER_SUCCESS(HttpStatus.OK, "주문 수락 성공"),
    GET_RIDER_ORDER_DETAILS_SUCCESS(HttpStatus.OK, "주문 정보 조회 성공"),
    COOKED_SUCCESS(HttpStatus.OK,"조리완료"),
    UPLOAD_RIDER_IMAGE_SUCCESS(HttpStatus.CREATED,"배달 상태 촬영 업로드 성공"),
    GET_ORDER_REQUEST_SUCCESS(HttpStatus.OK, "배달 요청 조회 성공");

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
