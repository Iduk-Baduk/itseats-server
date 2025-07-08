package com.idukbaduk.itseats.member.dto.response;

import com.idukbaduk.itseats.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentMemberResponse {
    private Long memberId;
    private String username;
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private String memberType;
    private Integer reviewCount;
    private Integer favoriteCount;

    public static CurrentMemberResponse of(Member member, int reviewCount, int favoriteCount) {
        return CurrentMemberResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phone(member.getPhone())
                .memberType(member.getMemberType().name())
                .reviewCount(reviewCount)
                .favoriteCount(favoriteCount)
                .build();
    }
}