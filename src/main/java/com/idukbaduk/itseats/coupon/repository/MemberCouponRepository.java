package com.idukbaduk.itseats.coupon.repository;

import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
}
