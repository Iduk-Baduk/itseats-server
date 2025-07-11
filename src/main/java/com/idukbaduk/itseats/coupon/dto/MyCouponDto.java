package com.idukbaduk.itseats.coupon.dto;

import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyCouponDto {
    private CouponType couponType;
    private int minPrice;
    private int discountValue;
    private LocalDateTime issueDate;
    private LocalDateTime validDate;
    private boolean canUsed;
}
