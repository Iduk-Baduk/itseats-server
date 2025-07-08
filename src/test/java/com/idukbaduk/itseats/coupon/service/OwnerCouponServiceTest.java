package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponCreateRequest;
import com.idukbaduk.itseats.coupon.dto.StoreCouponCreateResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OwnerCouponServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private CouponRepository couponRepository;

    @InjectMocks
    private OwnerCouponService ownerCouponService;

    @Test
    @DisplayName("매장 쿠폰 등록 성공")
    void createStoreCoupon_success() {
        // given
        Long storeId = 1L;
        String username = "owner1";
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("3,000원 할인")
                .description("3000원 할인 쿠폰입니다")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(LocalDateTime.of(2025, 7, 8, 0, 0))
                .validDate(LocalDateTime.of(2025, 7, 31, 23, 59))
                .build();

        Member member = Member.builder()
                .username(username)
                .build();

        Store store = Store.builder()
                .storeId(storeId)
                .member(member)
                .build();

        Coupon savedCoupon = Coupon.builder()
                .couponId(11L)
                .store(store)
                .couponName(request.getName())
                .description(request.getDescription())
                .discountValue(request.getDiscountValue())
                .couponType(request.getCouponType())
                .quantity(request.getQuantity())
                .minPrice(request.getMinPrice())
                .issueStartDate(request.getIssueStartDate())
                .validDate(request.getValidDate())
                .build();

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
        given(couponRepository.save(any(Coupon.class))).willReturn(savedCoupon);

        // when
        StoreCouponCreateResponse response = ownerCouponService.createStoreCoupon(storeId, request, username);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getCouponId()).isEqualTo(11L);
        assertThat(response.getName()).isEqualTo("3,000원 할인");
        assertThat(response.getQuantity()).isEqualTo(100);
        assertThat(response.getCouponType()).isEqualTo(CouponType.FIXED);
        assertThat(response.getMinPrice()).isEqualTo(15000);
        assertThat(response.getDiscountValue()).isEqualTo(3000);
        assertThat(response.getIssueStartDate()).isEqualTo(LocalDateTime.of(2025, 7, 8, 0, 0));
        assertThat(response.getValidDate()).isEqualTo(LocalDateTime.of(2025, 7, 31, 23, 59));

        // DB 저장 검증
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).save(couponCaptor.capture());
        Coupon capturedCoupon = couponCaptor.getValue();
        assertThat(capturedCoupon.getStore()).isEqualTo(store);
        assertThat(capturedCoupon.getCouponName()).isEqualTo("3,000원 할인");
        assertThat(capturedCoupon.getDiscountValue()).isEqualTo(3000);
        assertThat(capturedCoupon.getCouponType()).isEqualTo(CouponType.FIXED);
        assertThat(capturedCoupon.getQuantity()).isEqualTo(100);
        assertThat(capturedCoupon.getMinPrice()).isEqualTo(15000);
    }

    @Test
    @DisplayName("가맹점주가 아니면 예외 발생")
    void createStoreCoupon_notStoreOwner() {
        // given
        Long storeId = 1L;
        String username = "owner1";
        CouponCreateRequest request = CouponCreateRequest.builder().build();

        Member other = Member.builder().username("other").build();
        Store store = Store.builder().storeId(storeId).member(other).build();

        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createStoreCoupon(storeId, request, username))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.NOT_STORE_OWNER.getMessage());
    }

    @Test
    @DisplayName("매장이 없으면 예외 발생")
    void createStoreCoupon_storeNotFound() {
        // given
        Long storeId = 1L;
        String username = "owner1";
        CouponCreateRequest request = CouponCreateRequest.builder().build();

        given(storeRepository.findById(storeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createStoreCoupon(storeId, request, username))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.STORE_NOT_FOUND.getMessage());
    }
}
