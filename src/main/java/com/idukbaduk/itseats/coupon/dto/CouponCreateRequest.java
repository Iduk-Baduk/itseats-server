package com.idukbaduk.itseats.coupon.dto;

import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "쿠폰 이름은 필수입니다.")
    private String name;

    private String description;

    @NotNull(message = "수량은 필수입니다.")
    private int quantity;

    @NotNull(message = "쿠폰 타입은 필수입니다.")
    private CouponType couponType;

    @NotNull(message = "최소 주문 금액은 필수입니다.")
    @Min(value = 0, message = "최소 주문 금액은 0원 이상이어야 합니다.")
    private int minPrice;

    @NotNull(message = "할인 금액/율은 필수입니다.")
    @Min(value = 1, message = "할인 금액/율은 1 이상이어야 합니다.")
    private int discountValue;

    @NotNull(message = "발급 시작일은 필수입니다.")
    @FutureOrPresent(message = "발급 시작일은 현재 이후여야 합니다.")
    private LocalDateTime issueStartDate;

    @NotNull(message = "발급 종료일은 필수 입니다.")
    private LocalDateTime issueEndDate;

    @NotNull(message = "만료일은 필수입니다.")
    private LocalDateTime validDate;
}
