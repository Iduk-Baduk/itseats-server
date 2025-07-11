package com.idukbaduk.itseats.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class MyCouponListResponse {
    private List<MyCouponDto> myCouponDtos;
}
