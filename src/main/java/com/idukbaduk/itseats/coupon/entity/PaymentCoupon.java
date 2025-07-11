package com.idukbaduk.itseats.coupon.entity;

import com.idukbaduk.itseats.global.BaseEntity;
import com.idukbaduk.itseats.payment.entity.Payment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "payment_coupon")
public class PaymentCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_coupon_id")
    private Long paymentCouponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "used_coupon_id", nullable = false)
    private MemberCoupon usedCoupon;
}
