package com.idukbaduk.itseats.review.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum ReviewResponse implements Response {

    GET_STORE_REVIEWS(HttpStatus.OK, "가게 별 리뷰 조회 성공"),
    GET_STORE_REVIEWS_BY_PERIOD(HttpStatus.OK, "특정 기간에 해당하는 리뷰 조회 성공"),
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
