package com.idukbaduk.itseats.coupon.repository;

import com.idukbaduk.itseats.coupon.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    @Modifying
    @Transactional
    @Query("UPDATE Coupon c SET c.issuedCount = c.issuedCount + 1 " +
            "WHERE c.couponId = :couponId AND c.issuedCount < c.quantity")
    int increaseIssuedCountIfNotExceeded(@Param("couponId") Long couponId);
}
