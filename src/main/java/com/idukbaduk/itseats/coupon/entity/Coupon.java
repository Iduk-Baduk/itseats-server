package com.idukbaduk.itseats.coupon.entity;

import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.entity.enums.TargetType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "coupon")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @Column(name = "discount_value", nullable = false)
    private int discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false)
    private CouponType couponType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "min_price", nullable = false)
    private int minPrice;

    @Column(name = "max_discount_value", nullable = false)
    private int maxDiscountValue;

    @Column(name = "issue_start_date", nullable = false)
    private LocalDateTime issueStartDate;

    @Column(name = "valid_date", nullable = false)
    private LocalDateTime validDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;
}
