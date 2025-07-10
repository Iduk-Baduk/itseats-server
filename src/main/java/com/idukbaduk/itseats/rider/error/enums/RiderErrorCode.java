package com.idukbaduk.itseats.rider.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RiderErrorCode implements ErrorCode {

    RIDER_NOT_FOUND(HttpStatus.NOT_FOUND, "라이더 조회 실패"),
    RIDER_ASSIGNMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "라이더 주문 할당 정보 조회 실패"),
    RIDER_ASSIGNMENT_STATUS_UPDATE_FAIL(HttpStatus.CONFLICT, "라이더 배차 관리 상태 변경 실패"),
    RIDER_LOCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "라이더 위치 정보가 없습니다."),
    NEARBY_ORDERS_NOT_FOUND(HttpStatus.NOT_FOUND, "현재 주변에 배정 가능한 배달이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[RIDER ERROR] " + message;
    }
}
