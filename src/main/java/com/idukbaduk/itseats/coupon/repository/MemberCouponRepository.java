package com.idukbaduk.itseats.coupon.repository;

import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    boolean existsByMemberAndCoupon(Member member, Coupon coupon);

    Long countByCoupon(Coupon coupon);
}

