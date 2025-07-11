package com.idukbaduk.itseats.coupon.repository;

import com.idukbaduk.itseats.coupon.entity.PaymentCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentCouponRepository extends JpaRepository<PaymentCoupon, Long> {
}
