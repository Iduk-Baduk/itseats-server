package com.idukbaduk.itseats.coupon.entity;

import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.entity.enums.TargetType;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.store.entity.Franchise;
import com.idukbaduk.itseats.store.entity.Store;
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
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "franchise_id")
    private Franchise franchise;

    @Column(name = "coupon_name", nullable = false)
    private String couponName;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "discount_value", nullable = false)
    private int discountValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "coupon_type", nullable = false)
    private CouponType couponType;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "min_price", nullable = false)
    private int minPrice;

    @Column(name = "max_discount_value")
    private int maxDiscountValue;

    @Column(name = "issue_start_date", nullable = false)
    private LocalDateTime issueStartDate;  // 발급 시작 일시

    @Column(name = "issue_end_date", nullable = false)
    private LocalDateTime issueEndDate;  // 발급 종료 일시

    @Column(name = "valid_date", nullable = false)
    private LocalDateTime validDate;  // 쿠폰 유효 기간

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false)
    private TargetType targetType;

    @Column(name = "issued_count", nullable = false)
    private int issuedCount = 0;
}
