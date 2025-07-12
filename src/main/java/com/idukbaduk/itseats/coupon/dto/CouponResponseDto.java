package com.idukbaduk.itseats.coupon.dto;

import com.idukbaduk.itseats.coupon.entity.Coupon;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CouponResponseDto {
    private Long couponId;
    private String name;
    private int discountValue;
    private int minPrice;
    private LocalDateTime issueStartDate;
    private LocalDateTime issueEndDate;
    private LocalDateTime validDate;

    public static CouponResponseDto of(Coupon coupon) {
        return CouponResponseDto.builder()
                .couponId(coupon.getCouponId())
                .name(coupon.getCouponName())
                .discountValue(coupon.getDiscountValue())
                .minPrice(coupon.getMinPrice())
                .issueStartDate(coupon.getIssueStartDate())
                .issueEndDate(coupon.getIssueEndDate())
                .validDate(coupon.getValidDate())
                .build();
    }
}
