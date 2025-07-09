package com.idukbaduk.itseats.coupon.repository;

import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {
    List<MemberCoupon> findAllByMember(Member member);
}
