package com.idukbaduk.itseats.external.jwt.entity.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JwtTokenType {

    ACCESS_TOKEN("access-Token"),
    REFRESH_TOKEN("refresh-Token");

    private final String value;

}
