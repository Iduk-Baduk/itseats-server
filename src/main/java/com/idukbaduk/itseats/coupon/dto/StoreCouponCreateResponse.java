package com.idukbaduk.itseats.coupon.dto;

import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StoreCouponCreateResponse {
    private Long couponId;
    private String name;
    private int quantity;
    private CouponType couponType;
    private int minPrice;
    private int discountValue;
    private LocalDateTime issueStartDate;
    private LocalDateTime issueEndDate;
    private LocalDateTime validDate;
}
