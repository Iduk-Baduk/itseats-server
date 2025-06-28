package com.idukbaduk.itseats.external.jwt.entity.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum JwtTokenType {

    ACCESS_TOKEN("access-token"),
    REFRESH_TOKEN("refresh-token");

    private final String value;

}
