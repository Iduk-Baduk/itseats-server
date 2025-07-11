package com.idukbaduk.itseats.external.jwt.dto;

import com.idukbaduk.itseats.external.jwt.entity.JwtToken;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record JwtTokenPair(
        JwtToken accessToken,
        JwtToken refreshToken
) {
    public static JwtTokenPair of(JwtToken accessToken, JwtToken refreshToken) {
        return JwtTokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
