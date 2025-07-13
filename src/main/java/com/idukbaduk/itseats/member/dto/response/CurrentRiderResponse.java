package com.idukbaduk.itseats.member.dto.response;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.store.dto.PointDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CurrentRiderResponse {
    private Long memberId;
    private String username;
    private String name;
    private String nickname;
    private String email;
    private String phone;
    private String memberType;

    private String deliveryMethod;
    private Boolean isWorking;
    private PointDto location;
    private String preferredArea;

    public static CurrentRiderResponse of(Member member, Rider rider) {
        return CurrentRiderResponse.builder()
                .memberId(member.getMemberId())
                .username(member.getUsername())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .phone(member.getPhone())
                .memberType(member.getMemberType().name())
                .deliveryMethod(rider.getDeliveryMethod().name())
                .isWorking(rider.getIsWorking())
                .location(new PointDto(rider.getLocation()))
                .preferredArea(rider.getPreferredArea())
                .build();
    }
}
