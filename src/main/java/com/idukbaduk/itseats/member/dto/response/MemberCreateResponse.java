package com.idukbaduk.itseats.member.dto.response;

import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PRIVATE)
public record MemberCreateResponse(
        Long memberId
)
{
    public static MemberCreateResponse of(Long memberId) {
        return MemberCreateResponse.builder()
                .memberId(memberId)
                .build();
    }
}
