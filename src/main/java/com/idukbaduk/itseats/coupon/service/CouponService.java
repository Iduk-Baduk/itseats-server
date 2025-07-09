package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.MyCouponDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponListResponse;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;

    public MyCouponListResponse getMyCoupons(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<MemberCoupon> myCoupons = memberCouponRepository.findAllByMember(member);

        LocalDateTime now = LocalDateTime.now();

        List<MyCouponDto> couponDtos = myCoupons.stream()
                .map(mc -> MyCouponDto.builder()
                        .couponType(mc.getCoupon().getCouponType())
                        .minPrice(mc.getCoupon().getMinPrice())
                        .discountValue(mc.getCoupon().getDiscountValue())
                        .issueDate(mc.getIssueDate())
                        .validDate(mc.getValidDate())
                        .canUsed(!mc.getIsUsed() && now.isBefore(mc.getValidDate()) && now.isAfter(mc.getIssueDate()))
                        .build())
                .toList();

        return MyCouponListResponse.builder().myCouponDtos(couponDtos).build();
    }
}
