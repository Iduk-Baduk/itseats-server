package com.idukbaduk.itseats.member.dto.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record CustomerCreateResponse(
        Long memberId,
        Boolean isSucceed
)
{
    public static CustomerCreateResponse of(Long memberId, Boolean isSucceed) {
        return CustomerCreateResponse.builder()
                .memberId(memberId)
                .isSucceed(isSucceed)
                .build();
    }
}
