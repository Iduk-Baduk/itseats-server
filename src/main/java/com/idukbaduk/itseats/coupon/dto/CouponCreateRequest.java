package com.idukbaduk.itseats.coupon.dto;

import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponCreateRequest {
    private String name;
    private String description;
    private int quantity;
    private CouponType couponType;
    private int minPrice;
    private int discountValue;
    private LocalDateTime issueStartDate;
    private LocalDateTime validDate;
}
