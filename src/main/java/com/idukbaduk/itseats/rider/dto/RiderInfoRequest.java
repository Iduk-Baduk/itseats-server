package com.idukbaduk.itseats.rider.dto;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.entity.enums.DeliveryMethod;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RiderInfoRequest {

    private DeliveryMethod deliveryMethod;
    private String preferredArea;

    public Rider toRider(Member member) {
        return Rider.builder()
                .member(member)
                .deliveryMethod(deliveryMethod)
                .isWorking(true)
                .preferredArea(preferredArea)
                .build();
    }
}
