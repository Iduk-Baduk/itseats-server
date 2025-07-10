package com.idukbaduk.itseats.coupon.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponIssueResponse {
    private Long memberCouponId;
    private Long couponId;
    private String name;
    private int discountValue;
    private int minPrice;
    private LocalDateTime issueDate;
    private LocalDateTime validDate;
    private boolean isUsed;
}
