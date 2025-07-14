package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponResponseDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponListResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.dto.CouponIssueResponse;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
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

import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CouponServiceTest {

    @Mock
    private CouponRepository couponRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MemberCouponRepository memberCouponRepository;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;

    @InjectMocks
    private CouponService couponService;

    @BeforeEach
    void setUp() {
        given(redissonClient.getLock(anyString())).willReturn(rLock);
        try {
            given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(true);
            given(rLock.isHeldByCurrentThread()).willReturn(true);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("테스트 설정 중 인터럽트 발생", e);
        }
    }

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

    @Test
    @DisplayName("쿠폰 발급 성공")
    void issueCoupon_success() {
        // given
        Long couponId = 99L;
        String username = "user1";
        Member member = Member.builder().memberId(1L).username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .couponName("3,000원 할인")
                .discountValue(3000)
                .minPrice(15000)
                .quantity(300)
                .issuedCount(99)
                .issueStartDate(LocalDateTime.now().minusDays(1))
                .issueEndDate(LocalDateTime.now().plusDays(7))
                .validDate(LocalDateTime.now().plusDays(30))
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(memberCouponRepository.existsByMemberAndCoupon(member, coupon)).willReturn(false);
        given(memberCouponRepository.countByCoupon(coupon)).willReturn(99L);
        given(couponRepository.increaseIssuedCountIfNotExceeded(couponId)).willReturn(1);

        given(memberCouponRepository.save(any(MemberCoupon.class)))
                .willAnswer(invocation -> {
                    MemberCoupon mc = invocation.getArgument(0);
                    return MemberCoupon.builder()
                            .memberCouponId(121L)
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
        assertThat(response.getMemberCouponId()).isEqualTo(121L);
        assertThat(response.getCouponId()).isEqualTo(99L);
        assertThat(response.getName()).isEqualTo("3,000원 할인");
        assertThat(response.getDiscountValue()).isEqualTo(3000);
        assertThat(response.getMinPrice()).isEqualTo(15000);
        assertThat(response.isUsed()).isFalse();

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("락 획득 실패 시 예외 발생")
    void issueCoupon_lockAcquisitionFailed() throws InterruptedException {
        // given
        Long couponId = 10L;
        String username = "user1";

        given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).willReturn(false);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.LOCK_ACQUISITION_FAILED.getMessage());
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰이면 예외 발생")
    void issueCoupon_alreadyIssued() {
        // given
        Long couponId = 10L;
        String username = "user1";
        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .issueStartDate(LocalDateTime.now().minusDays(1))
                .issueEndDate(LocalDateTime.now().plusDays(7))
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(memberCouponRepository.existsByMemberAndCoupon(member, coupon)).willReturn(true);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.ALREADY_ISSUED.getMessage());

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("발급 수량 초과 시 예외 발생")
    void issueCoupon_quantityExceeded() {
        // given
        Long couponId = 10L;
        String username = "user1";
        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .quantity(100)
                .issuedCount(100)    // 이미 최대 수량만큼 발급됨
                .issueStartDate(LocalDateTime.now().minusDays(1))
                .issueEndDate(LocalDateTime.now().plusDays(7))
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));
        given(memberCouponRepository.existsByMemberAndCoupon(member, coupon)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.QUANTITY_EXCEEDED.getMessage());

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("유효기간 시작 전 발급 시 예외 발생")
    void issueCoupon_beforeIssueStartDate() {
        // given
        Long couponId = 10L;
        String username = "user1";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureStartDate = now.plusDays(1);

        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .quantity(100)
                .issueStartDate(futureStartDate)
                .issueEndDate(futureStartDate.plusDays(7))
                .validDate(futureStartDate.plusDays(30))
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_PERIOD.getMessage());

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("유효기간 종료 후 발급 시 예외 발생")
    void issueCoupon_afterIssueEndDate() {
        // given
        Long couponId = 10L;
        String username = "user1";

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastEndDate = now.minusDays(1);

        Member member = Member.builder().username(username).build();
        Coupon coupon = Coupon.builder()
                .couponId(couponId)
                .quantity(100)
                .issueStartDate(pastEndDate.minusDays(7))
                .issueEndDate(pastEndDate)
                .validDate(now.plusDays(30))
                .build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.of(coupon));

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_PERIOD.getMessage());

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 예외 발생")
    void issueCoupon_memberNotFound() {
        // given
        Long couponId = 10L;
        String username = "nonexistent";

        given(memberRepository.findByUsername(username)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("존재하지 않는 쿠폰일 경우 예외 발생")
    void issueCoupon_couponNotFound() {
        // given
        Long couponId = 999L;
        String username = "user1";
        Member member = Member.builder().username(username).build();

        given(memberRepository.findByUsername(username)).willReturn(Optional.of(member));
        given(couponRepository.findById(couponId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.COUPON_NOT_FOUND.getMessage());

        verify(rLock).unlock();
    }

    @Test
    @DisplayName("락 획득 중 인터럽트 발생 시 예외 처리")
    void issueCoupon_lockInterrupted() throws InterruptedException {
        // given
        Long couponId = 10L;
        String username = "user1";

        given(redissonClient.getLock(anyString())).willReturn(rLock);
        given(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .willThrow(new InterruptedException());
        given(rLock.isHeldByCurrentThread()).willReturn(false); // 인터럽트 시 락 미보유

        // when & then
        assertThatThrownBy(() -> couponService.issueCoupon(couponId, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.LOCK_INTERRUPTED.getMessage());

        // 인터럽트 발생 시 락 해제 시도하지 않음을 확인
        verify(rLock, never()).unlock();
    }

    @Test
    @DisplayName("전체 쿠폰 목록 정상 조회 - 발급 여부 포함")
    void getAllCoupons_success() {
        String username = "testuser";
        Member member = Member.builder().memberId(1L).username(username).build();

        Coupon coupon1 = Coupon.builder()
                .couponId(1L)
                .couponName("Coupon1")
                .discountValue(1000)
                .minPrice(5000)
                .issueStartDate(LocalDateTime.now().minusDays(1))
                .issueEndDate(LocalDateTime.now().plusDays(5))
                .validDate(LocalDateTime.now().plusDays(30))
                .build();

        Coupon coupon2 = Coupon.builder()
                .couponId(2L)
                .couponName("Coupon2")
                .discountValue(2000)
                .minPrice(10000)
                .issueStartDate(LocalDateTime.now().minusDays(1))
                .issueEndDate(LocalDateTime.now().plusDays(5))
                .validDate(LocalDateTime.now().plusDays(30))
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(couponRepository.findAll()).thenReturn(List.of(coupon1, coupon2));
        when(memberCouponRepository.existsByMemberAndCoupon(member, coupon1)).thenReturn(true);
        when(memberCouponRepository.existsByMemberAndCoupon(member, coupon2)).thenReturn(false);

        List<CouponResponseDto> result = couponService.getAllCoupons(username);

        assertThat(result).hasSize(2);

        CouponResponseDto dto1 = result.get(0);
        CouponResponseDto dto2 = result.get(1);

        assertThat(dto1.getCouponId()).isEqualTo(1L);
        assertThat(dto1.getName()).isEqualTo("Coupon1");
        assertThat(dto1.isIssued()).isTrue();

        assertThat(dto2.getCouponId()).isEqualTo(2L);
        assertThat(dto2.getName()).isEqualTo("Coupon2");
        assertThat(dto2.isIssued()).isFalse();
    }
}
