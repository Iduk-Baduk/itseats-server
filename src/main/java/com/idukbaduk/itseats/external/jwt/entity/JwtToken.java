package com.idukbaduk.itseats.external.jwt.entity;

import com.idukbaduk.itseats.external.jwt.entity.enums.JwtTokenType;
import java.time.Duration;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record JwtToken(
        JwtTokenType type,
        String value,
        Duration duration
) {
    public static JwtToken of(JwtTokenType type, String value, Duration duration) {
        return JwtToken.builder()
                .type(type)
                .value(value)
                .duration(duration)
                .build();
    }
}
