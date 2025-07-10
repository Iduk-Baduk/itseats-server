package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.member.entity.Member;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CouponPolicyService {

    public int applyCouponDiscount(MemberCoupon memberCoupon, Member member, int orderPrice) {
        validateCoupon(memberCoupon, member, orderPrice);
        return calculateDiscount(memberCoupon.getCoupon(), orderPrice);
    }

    private void validateCoupon(MemberCoupon memberCoupon, Member member, int orderPrice) {
        if (memberCoupon == null || !memberCoupon.getMember().equals(member)) {
            throw new CouponException(CouponErrorCode.COUPON_NOT_FOUND);
        }
        if (memberCoupon.getIsUsed()) {
            throw new CouponException(CouponErrorCode.COUPON_ALREADY_USED);
        }
        if (LocalDateTime.now().isAfter(memberCoupon.getValidDate())) {
            throw new CouponException(CouponErrorCode.COUPON_EXPIRED);
        }
        if (orderPrice < memberCoupon.getCoupon().getMinPrice()) {
            throw new CouponException(CouponErrorCode.INSUFFICIENT_ORDER_AMOUNT);
        }
    }

    private int calculateDiscount(Coupon coupon, int orderPrice) {
        if (coupon.getCouponType() == CouponType.FIXED) {
            return Math.min(coupon.getDiscountValue(), orderPrice);
        }
        if (coupon.getCouponType() == CouponType.RATE) {
            int discount = orderPrice * coupon.getDiscountValue() / 100;
            return Math.min(discount, orderPrice);
        }
        throw new CouponException(CouponErrorCode.COUPON_NOT_FOUND);
    }
}
