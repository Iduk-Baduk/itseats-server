package com.idukbaduk.itseats.order.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    MENU_OPTION_SERIALIZATION_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "menuOption 직렬화 실패"),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문 조회 실패"),
    ORDER_STATUS_UPDATE_FAIL(HttpStatus.CONFLICT, "주문 상태 변경 실패"),
    ORDER_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "해당 주문은 이미 라이더가 배정되었습니다."),
    INVALID_USERNAME(HttpStatus.NOT_FOUND, "유효하지 않은 사용자 이름입니다."),
    INVALID_ORDER_STATUS(HttpStatus.CONFLICT, "잘못된 주문 상태입니다."),
    REQUIRED_RIDER_IMAGE(HttpStatus.BAD_REQUEST, "배달 상태 이미지는 필수입니다."),
    RIDER_IMAGE_IO_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "배달 상태 이미지 파일 저장 중 IO 오류가 발생하였습니다.");

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[ORDER ERROR] " + message;
    }
}
