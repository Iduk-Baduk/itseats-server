package com.idukbaduk.itseats.member.dto.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record CustomerCreateResponse(
        Long memberId
)
{
    public static CustomerCreateResponse of(Long memberId) {
        return CustomerCreateResponse.builder()
                .memberId(memberId)
                .build();
    }
}
