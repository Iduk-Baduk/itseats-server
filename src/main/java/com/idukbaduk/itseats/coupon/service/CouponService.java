package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponResponseDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponListResponse;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.dto.CouponIssueResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final RedissonClient redissonClient;

    private static final String COUPON_LOCK_PREFIX = "coupon:lock:";
    private static final Long WAIT_TIME = 5L;
    private static final Long LEASE_TIME = 3L;

    @Transactional(readOnly = true)
    public MyCouponListResponse getMyCoupons(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<MemberCoupon> myCoupons = memberCouponRepository.findAllByMember(member);

        List<MyCouponDto> couponDtos = myCoupons.stream()
                .map(mc -> {
                    Coupon coupon = mc.getCoupon();
                    Long storeId = coupon.getStore() != null ? coupon.getStore().getStoreId() : null;

                    return MyCouponDto.builder()
                            .couponType(coupon.getCouponType())
                            .minPrice(coupon.getMinPrice())
                            .discountValue(coupon.getDiscountValue())
                            .issueDate(mc.getIssueDate())
                            .validDate(mc.getValidDate())
                            .canUsed(canUse(mc))
                            .storeId(storeId)
                            .build();
                })
                .toList();

        return MyCouponListResponse.builder().myCouponDtos(couponDtos).build();
    }

    private boolean canUse(MemberCoupon memberCoupon) {
        return !memberCoupon.getIsUsed()
                && LocalDateTime.now().isBefore(memberCoupon.getValidDate())
                && LocalDateTime.now().isAfter(memberCoupon.getIssueDate());
    }

    @Transactional
    public CouponIssueResponse issueCoupon(Long couponId, String username) {
        RLock lock = redissonClient.getLock(COUPON_LOCK_PREFIX + couponId);

        try {
            // 락 획득 시도 (5초 대기, 3초 후 자동 해제)
            boolean isLocked = lock.tryLock(WAIT_TIME, LEASE_TIME, TimeUnit.SECONDS);
            if (!isLocked) {
                throw new CouponException(CouponErrorCode.LOCK_ACQUISITION_FAILED);
            }

            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

            validateIssuePeriod(coupon);

            if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
                throw new CouponException(CouponErrorCode.ALREADY_ISSUED);
            }

            if (!coupon.canIssue()) {
                throw new CouponException(CouponErrorCode.QUANTITY_EXCEEDED);
            }
            coupon.increaseIssuedCount();

            MemberCoupon memberCoupon = MemberCoupon.builder()
                    .member(member)
                    .coupon(coupon)
                    .isUsed(false)
                    .issueDate(LocalDateTime.now())
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

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CouponException(CouponErrorCode.LOCK_INTERRUPTED);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    @Transactional(readOnly = true)
    public List<CouponResponseDto> getAllCoupons(String username) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        List<Coupon> coupons = couponRepository.findAll();

        return coupons.stream()
                .map(coupon -> {
                    boolean isIssued = memberCouponRepository.existsByMemberAndCoupon(member, coupon);
                    return CouponResponseDto.of(coupon, isIssued);
                })
                .toList();
    }


    private void validateIssuePeriod(Coupon coupon) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getIssueStartDate()) || now.isAfter(coupon.getIssueEndDate())) {
            throw new CouponException(CouponErrorCode.INVALID_PERIOD);
        }
    }
}
