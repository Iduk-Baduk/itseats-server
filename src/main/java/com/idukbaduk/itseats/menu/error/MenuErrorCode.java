package com.idukbaduk.itseats.menu.error;

import com.idukbaduk.itseats.global.error.core.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MenuErrorCode implements ErrorCode {
    MENU_NOT_FOUND(HttpStatus.NOT_FOUND,"메뉴를 찾을 수 없습니다."),
    MENU_NOT_BELONG_TO_STORE(HttpStatus.NOT_FOUND,"해당 메뉴는 요청한 가맹점에 속해있지 않습니다."),
    MENU_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "메뉴 그룹을 찾을 수 없습니다."),
    OPTION_GROUP_NAME_DUPLICATED(HttpStatus.BAD_REQUEST, "옵션 그룹 이름은 중복될 수 없습니다."),
    OPTION_GROUP_RANGE_INVALID(HttpStatus.BAD_REQUEST, "옵션 그룹 최대 선택은 최소 선택 이상이어야 합니다."),
    MENU_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 메뉴에 접근할 권한이 없습니다.")
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
