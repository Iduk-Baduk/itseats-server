package com.idukbaduk.itseats.rider.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum RiderResponse implements Response {

    MODIFY_IS_WORKING_SUCCESS(HttpStatus.OK, "출/퇴근 상태전환 성공"),
    UPDATE_STATUS_ACCEPT_SUCCESS(HttpStatus.OK, "배달 수락 완료"),
    UPDATE_STATUS_ARRIVED_SUCCESS(HttpStatus.OK, "매장 도착 완료"),
    UPDATE_STATUS_PICKUP_SUCCESS(HttpStatus.OK, "픽업 완료"),
    UPDATE_STATUS_DELIVERED_SUCCESS(HttpStatus.OK, "배달 완료");

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
