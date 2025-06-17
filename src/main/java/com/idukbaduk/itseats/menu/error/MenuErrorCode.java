package com.idukbaduk.itseats.menu.error;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MenuErrorCode implements ErrorCode {
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND,"메뉴를 찾을 수 없습니다."),
    INVALID_MENU_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 메뉴 요청입니다."),
    STORE_ID_REQUIRED(HttpStatus.BAD_REQUEST, "storeId는 필수입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return httpStatus;
    }

    @Override
    public String getMessage() {
        return "[MENU ERROR] " + message;
    }
}