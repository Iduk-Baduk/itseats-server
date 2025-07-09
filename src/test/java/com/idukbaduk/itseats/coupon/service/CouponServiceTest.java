package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.MyCouponDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponListResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("내 쿠폰 목록 정상 조회")
    void getMyCoupons_success() {
        Member member = Member.builder()
                .memberId(1L)
                .username("testuser")
                .build();

        Coupon coupon1 = Coupon.builder()
                .couponType(CouponType.FIXED)
                .minPrice(10000)
                .discountValue(1000)
                .build();
        Coupon coupon2 = Coupon.builder()
                .couponType(CouponType.RATE)
                .minPrice(20000)
                .discountValue(10)
                .build();

        LocalDateTime now = LocalDateTime.now();
        MemberCoupon mc1 = MemberCoupon.builder()
                .coupon(coupon1)
                .isUsed(false)
                .issueDate(now.minusDays(1))
                .validDate(now.plusDays(2))
                .build();
        MemberCoupon mc2 = MemberCoupon.builder()
                .coupon(coupon2)
                .isUsed(true)
                .issueDate(now.minusDays(2))
                .validDate(now.plusDays(1))
                .build();

        when(memberRepository.findByUsername("testuser")).thenReturn(Optional.of(member));
        when(memberCouponRepository.findAllByMember(member)).thenReturn(List.of(mc1, mc2));

        MyCouponListResponse response = couponService.getMyCoupons("testuser");

        assertThat(response.getMyCouponDtos()).hasSize(2);
        MyCouponDto dto1 = response.getMyCouponDtos().get(0);
        MyCouponDto dto2 = response.getMyCouponDtos().get(1);

        assertThat(dto1.getCouponType()).isEqualTo(CouponType.FIXED);
        assertThat(dto1.isCanUsed()).isTrue();
        assertThat(dto2.getCouponType()).isEqualTo(CouponType.RATE);
        assertThat(dto2.isCanUsed()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 회원 예외 발생")
    void getMyCoupons_memberNotFound() {
        when(memberRepository.findByUsername("notfound")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> couponService.getMyCoupons("notfound"))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }
}
