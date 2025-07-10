package com.idukbaduk.itseats.coupon.service;

import com.idukbaduk.itseats.coupon.dto.CouponCreateRequest;
import com.idukbaduk.itseats.coupon.dto.FranchiseCouponCreateResponse;
import com.idukbaduk.itseats.coupon.dto.StoreCouponCreateResponse;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.entity.enums.TargetType;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.CouponRepository;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.entity.Franchise;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.FranchiseRepository;
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
    @Mock
    private FranchiseRepository franchiseRepository;

    @InjectMocks
    private OwnerCouponService ownerCouponService;

    @Test
    @DisplayName("매장 쿠폰 등록 성공")
    void createStoreCoupon_success() {
        // given
        Long storeId = 1L;
        String username = "owner1";
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("3,000원 할인")
                .description("3000원 할인 쿠폰입니다")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
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
                .issueEndDate(request.getIssueEndDate())
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
        assertThat(response.getIssueStartDate()).isEqualTo(now.plusDays(1));
        assertThat(response.getIssueEndDate()).isEqualTo(now.plusDays(7));
        assertThat(response.getValidDate()).isEqualTo(now.plusDays(30));

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
        assertThat(capturedCoupon.getIssueStartDate()).isEqualTo(now.plusDays(1));
        assertThat(capturedCoupon.getIssueEndDate()).isEqualTo(now.plusDays(7));
        assertThat(capturedCoupon.getValidDate()).isEqualTo(now.plusDays(30));
    }

    @Test
    @DisplayName("발급 종료일이 발급 시작일 이전이면 예외 발생")
    void createStoreCoupon_issueEndBeforeStart() {
        // given
        Long storeId = 1L;
        String username = "owner1";
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("3,000원 할인")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(now.plusDays(5))
                .issueEndDate(now.plusDays(2)) // 종료일이 시작일보다 이전
                .validDate(now.plusDays(30))
                .build();

        Member member = Member.builder().username(username).build();
        Store store = Store.builder().storeId(storeId).member(member).build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createStoreCoupon(storeId, request, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_DATE_RANGE.getMessage());
    }

    @Test
    @DisplayName("만료일이 발급 종료일 이전이면 예외 발생")
    void createStoreCoupon_validDateBeforeIssueEnd() {
        // given
        Long storeId = 1L;
        String username = "owner1";
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("3,000원 할인")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(10))
                .validDate(now.plusDays(5)) // 만료일이 발급 종료일보다 이전
                .build();

        Member member = Member.builder().username(username).build();
        Store store = Store.builder().storeId(storeId).member(member).build();
        given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createStoreCoupon(storeId, request, username))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_DATE_RANGE.getMessage());
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

    @Test
    @DisplayName("프랜차이즈 쿠폰 등록 성공")
    void createFranchiseCoupon_success() {
        // given
        Long franchiseId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("5,000원 할인")
                .description("5000원 할인 쿠폰입니다")
                .quantity(200)
                .couponType(CouponType.FIXED)
                .minPrice(20000)
                .discountValue(5000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
                .build();

        Franchise franchise = Franchise.builder()
                .franchiseId(franchiseId)
                .brandName("테스트 프랜차이즈")
                .build();

        Coupon savedCoupon = Coupon.builder()
                .couponId(12L)
                .franchise(franchise)
                .couponName(request.getName())
                .description(request.getDescription())
                .discountValue(request.getDiscountValue())
                .couponType(request.getCouponType())
                .quantity(request.getQuantity())
                .minPrice(request.getMinPrice())
                .issueStartDate(request.getIssueStartDate())
                .issueEndDate(request.getIssueEndDate())
                .validDate(request.getValidDate())
                .targetType(TargetType.FRANCHISE)
                .build();

        given(franchiseRepository.findById(franchiseId)).willReturn(Optional.of(franchise));
        given(couponRepository.save(any(Coupon.class))).willReturn(savedCoupon);

        // when
        FranchiseCouponCreateResponse response = ownerCouponService.createFranchiseCoupon(franchiseId, request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getFranchiseName()).isEqualTo("테스트 프랜차이즈");
        assertThat(response.getCouponId()).isEqualTo(12L);
        assertThat(response.getName()).isEqualTo("5,000원 할인");
        assertThat(response.getQuantity()).isEqualTo(200);
        assertThat(response.getCouponType()).isEqualTo(CouponType.FIXED);
        assertThat(response.getMinPrice()).isEqualTo(20000);
        assertThat(response.getDiscountValue()).isEqualTo(5000);
        assertThat(response.getIssueStartDate()).isEqualTo(now.plusDays(1));
        assertThat(response.getIssueEndDate()).isEqualTo(now.plusDays(7));
        assertThat(response.getValidDate()).isEqualTo(now.plusDays(30));

        // DB 저장 검증
        ArgumentCaptor<Coupon> couponCaptor = ArgumentCaptor.forClass(Coupon.class);
        verify(couponRepository).save(couponCaptor.capture());
        Coupon capturedCoupon = couponCaptor.getValue();
        assertThat(capturedCoupon.getFranchise()).isEqualTo(franchise);
        assertThat(capturedCoupon.getTargetType()).isEqualTo(TargetType.FRANCHISE);
    }

    @Test
    @DisplayName("프랜차이즈가 없으면 예외 발생")
    void createFranchiseCoupon_franchiseNotFound() {
        // given
        Long franchiseId = 1L;
        CouponCreateRequest request = CouponCreateRequest.builder().build();

        given(franchiseRepository.findById(franchiseId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createFranchiseCoupon(franchiseId, request))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(StoreErrorCode.FRANCHISE_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("프랜차이즈 쿠폰 - 발급 종료일이 발급 시작일 이전이면 예외 발생")
    void createFranchiseCoupon_issueEndBeforeStart() {
        // given
        Long franchiseId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("5,000원 할인")
                .quantity(200)
                .couponType(CouponType.FIXED)
                .minPrice(20000)
                .discountValue(5000)
                .issueStartDate(now.plusDays(10))
                .issueEndDate(now.plusDays(5)) // 종료일이 시작일보다 이전
                .validDate(now.plusDays(30))
                .build();

        Franchise franchise = Franchise.builder()
                .franchiseId(franchiseId)
                .brandName("테스트 프랜차이즈")
                .build();

        given(franchiseRepository.findById(franchiseId)).willReturn(Optional.of(franchise));

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createFranchiseCoupon(franchiseId, request))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_DATE_RANGE.getMessage());
    }

    @Test
    @DisplayName("프랜차이즈 쿠폰 - 만료일이 발급 종료일 이전이면 예외 발생")
    void createFranchiseCoupon_validDateBeforeIssueEnd() {
        // given
        Long franchiseId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("5,000원 할인")
                .quantity(200)
                .couponType(CouponType.FIXED)
                .minPrice(20000)
                .discountValue(5000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(10))
                .validDate(now.plusDays(5)) // 만료일이 발급 종료일보다 이전
                .build();

        Franchise franchise = Franchise.builder()
                .franchiseId(franchiseId)
                .brandName("테스트 프랜차이즈")
                .build();

        given(franchiseRepository.findById(franchiseId)).willReturn(Optional.of(franchise));

        // when & then
        assertThatThrownBy(() -> ownerCouponService.createFranchiseCoupon(franchiseId, request))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.INVALID_DATE_RANGE.getMessage());
    }
}
