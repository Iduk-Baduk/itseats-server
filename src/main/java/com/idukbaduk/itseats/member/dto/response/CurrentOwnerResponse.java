package com.idukbaduk.itseats.member.dto.response;

import com.idukbaduk.itseats.member.dto.StoreIdAndStoreNameDto;
import com.idukbaduk.itseats.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CurrentOwnerResponse {
    private Long memberId;
    private String username;
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private String memberType;
    private List<StoreIdAndStoreNameDto> stores;

    public static CurrentOwnerResponse of(Member member, List<StoreIdAndStoreNameDto> stores) {
        return CurrentOwnerResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phone(member.getPhone())
                .memberType(member.getMemberType().name())
                .stores(stores)
                .build();
    }
}
