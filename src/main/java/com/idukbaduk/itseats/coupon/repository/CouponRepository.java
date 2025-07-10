package com.idukbaduk.itseats.coupon.repository;

import com.idukbaduk.itseats.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
