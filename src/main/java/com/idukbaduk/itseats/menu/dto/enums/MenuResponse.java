package com.idukbaduk.itseats.menu.dto.enums;

import com.idukbaduk.itseats.global.response.Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
public enum MenuResponse implements Response {

    GET_MENU_LIST_SUCCESS(HttpStatus.OK, "메뉴 목록 조회 성공"),
    GET_MENU_GROUP_SUCCESS(HttpStatus.OK, "메뉴 그룹 조회 성공"),
    SAVE_MENU_GROUP_SUCCESS(HttpStatus.OK, "메뉴 그룹 설정 성공"),
    CREATE_MENU_SUCCESS(HttpStatus.CREATED, "메뉴 추가 성공"),
    UPDATE_MENU_SUCCESS(HttpStatus.OK, "메뉴 수정 성공"),
    DELETE_MENU_SUCCESS(HttpStatus.CREATED, "메뉴 삭제 성공")
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
