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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberCouponRepository memberCouponRepository;

    @InjectMocks
    private CouponService couponService;

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_success() {
        // given
        Long couponId = 10L;
        String username = "user1";
        Member member = Member.builder().memberId(1L).username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .couponName("3,000원 할인")
                .discountValue(3000)
                .minPrice(15000)
                .quantity(100)
                .issueStartDate(LocalDateTime.of(2025, 7, 8, 0, 0))
                .issueEndDate(LocalDateTime.of(2025, 7, 15, 0, 0))
                .validDate(LocalDateTime.of(2025, 7, 31, 23, 59))
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(memberCouponRepository.existsByMemberAndCoupon(member, coupon)).willReturn(false);
        given(memberCouponRepository.countByCoupon(coupon)).willReturn(10L);

        given(memberCouponRepository.save(any(MemberCoupon.class)))
                .willAnswer(invocation -> {
                    MemberCoupon mc = invocation.getArgument(0);
                    return MemberCoupon.builder()
                            .memberCouponId(101L)
                            .member(mc.getMember())
                            .coupon(mc.getCoupon())
                            .isUsed(false)
                            .issueDate(mc.getIssueDate())
                            .validDate(mc.getValidDate())
                            .build();
                });

        // when
        CouponIssueResponse response = couponService.issueCoupon(couponId, username);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMemberCouponId()).isEqualTo(101L);
        assertThat(response.getCouponId()).isEqualTo(10L);
        assertThat(response.getName()).isEqualTo("3,000원 할인");
        assertThat(response.getDiscountValue()).isEqualTo(3000);
        assertThat(response.getMinPrice()).isEqualTo(15000);
        assertThat(response.isUsed()).isFalse();
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰이면 예외 발생")
    void issueCoupon_alreadyIssued() {
        // given
        Long couponId = 10L;
        String username = "user1";
        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder().couponId(couponId).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(memberCouponRepository.existsByMemberAndCoupon(member, coupon)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.ALREADY_ISSUED.getMessage());
    }

    @Test
    @DisplayName("발급 수량 초과 시 예외 발생")
    void issueCoupon_quantityExceeded() {
        // given
        Long couponId = 10L;
        String username = "user1";
        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder().couponId(couponId).quantity(100).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(memberCouponRepository.existsByMemberAndCoupon(member, coupon)).willReturn(false);
        given(memberCouponRepository.countByCoupon(coupon)).willReturn(100L);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.QUANTITY_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("유효기간 시작 전 발급 시 예외 발생")
    void issueCoupon_beforeIssueStartDate() {
        // given
        Long couponId = 10L;
        String username = "user1";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureStartDate = now.plusYears(1); // 현재보다 미래의 시점

        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .quantity(100)
                .issueStartDate(futureStartDate)  // 미래의 발급 시작일
                .validDate(futureStartDate.plusDays(30))
                .build();

        // 실제로 사용되는 stub만 정의
        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_PERIOD.getMessage());
    }
}
