package com.idukbaduk.itseats.external.jwt.entity.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JwtTokenType {

    ACCESS_TOKEN("Access-Token"),
    REFRESH_TOKEN("Refresh-Token");

    private final String value;

}
