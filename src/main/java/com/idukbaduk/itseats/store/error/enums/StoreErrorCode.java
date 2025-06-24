package com.idukbaduk.itseats.store.error.enums;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum StoreErrorCode implements ErrorCode {

    STORE_NOT_FOUND(HttpStatus.NOT_FOUND, "가맹점 조회 실패"),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "카테고리 조회 실패"),
    FRANCHISE_NOT_FOUND(HttpStatus.NOT_FOUND, "프렌차이즈 조회 실패"),
    FRANCHISE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "프랜차이즈 매장일 경우 franchiseId가 필요합니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[STORE ERROR] " + message;
    }
}
