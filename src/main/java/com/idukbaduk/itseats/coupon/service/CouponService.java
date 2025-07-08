package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponIssueResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public CouponIssueResponse issueCoupon(Long couponId, String username) {

        // TODO: 동시성 제어 (Redis 활용 분산 락, DB 락, 낙관적 락 등) 관련 추가
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

        if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
            throw new CouponException(CouponErrorCode.ALREADY_ISSUED);
        }

        Long issuedCount = memberCouponRepository.countByCoupon(coupon);
        if (issuedCount >= coupon.getQuantity()) {
            throw new CouponException(CouponErrorCode.QUANTITY_EXCEEDED);
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueStartDate()) || now.isAfter(coupon.getIssueEndDate())) {
            throw new CouponException(CouponErrorCode.INVALID_PERIOD);
        }

        MemberCoupon memberCoupon = MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .isUsed(false)
                .issueDate(now)
                .validDate(coupon.getValidDate())
                .build();

        MemberCoupon savedMemberCoupon = memberCouponRepository.save(memberCoupon);

        return CouponIssueResponse.builder()
                .memberCouponId(savedMemberCoupon.getMemberCouponId())
                .couponId(coupon.getCouponId())
                .name(coupon.getCouponName())
                .discountValue(coupon.getDiscountValue())
                .minPrice(coupon.getMinPrice())
                .issueDate(savedMemberCoupon.getIssueDate())
                .validDate(savedMemberCoupon.getValidDate())
                .isUsed(false)
                .build();
    }
}
