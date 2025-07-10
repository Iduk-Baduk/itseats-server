package com.idukbaduk.itseats.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.coupon.dto.CouponCreateRequest;
import com.idukbaduk.itseats.coupon.dto.FranchiseCouponCreateResponse;
import com.idukbaduk.itseats.coupon.dto.StoreCouponCreateResponse;
import com.idukbaduk.itseats.coupon.dto.enums.CouponResponse;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.service.OwnerCouponService;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerCouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerCouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerCouponService ownerCouponService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("매장 쿠폰 생성 성공")
    @WithMockUser(username = "owner")
    void createStoreCoupon_success() throws Exception {
        // given
        Long storeId = 1L;
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

        StoreCouponCreateResponse response = StoreCouponCreateResponse.builder()
                .couponId(11L)
                .name("3,000원 할인")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
                .build();

        given(ownerCouponService.createStoreCoupon(eq(storeId), any(CouponCreateRequest.class), any(String.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/owner/stores/{storeId}/coupons", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.couponId").value(11L))
                .andExpect(jsonPath("$.data.name").value("3,000원 할인"));
    }

    @Test
    @DisplayName("매장 쿠폰 생성 실패 - 매장 없음")
    @WithMockUser(username = "owner")
    void createStoreCoupon_storeNotFound() throws Exception {
        // given
        Long storeId = 99L;
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("3,000원 할인")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
                .build();

        doThrow(new StoreException(StoreErrorCode.STORE_NOT_FOUND))
                .when(ownerCouponService).createStoreCoupon(eq(storeId), any(CouponCreateRequest.class), any(String.class));

        // when & then
        mockMvc.perform(post("/api/owner/stores/{storeId}/coupons", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("프랜차이즈 쿠폰 생성 성공")
    @WithMockUser(username = "owner")
    void createFranchiseCoupon_success() throws Exception {
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

        FranchiseCouponCreateResponse response = FranchiseCouponCreateResponse.builder()
                .franchiseName("테스트 프랜차이즈")
                .couponId(12L)
                .name("5,000원 할인")
                .quantity(200)
                .couponType(CouponType.FIXED)
                .minPrice(20000)
                .discountValue(5000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
                .build();

        given(ownerCouponService.createFranchiseCoupon(eq(franchiseId), any(CouponCreateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/owner/franchises/{franchiseId}/coupons", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.franchiseName").value("테스트 프랜차이즈"))
                .andExpect(jsonPath("$.data.couponId").value(12L));
    }

    @Test
    @DisplayName("프랜차이즈 쿠폰 생성 실패 - 프랜차이즈 없음")
    @WithMockUser(username = "owner")
    void createFranchiseCoupon_franchiseNotFound() throws Exception {
        // given
        Long franchiseId = 99L;
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("5,000원 할인")
                .quantity(200)
                .couponType(CouponType.FIXED)
                .minPrice(20000)
                .discountValue(5000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
                .build();

        doThrow(new StoreException(StoreErrorCode.FRANCHISE_NOT_FOUND))
                .when(ownerCouponService).createFranchiseCoupon(eq(franchiseId), any(CouponCreateRequest.class));

        // when & then
        mockMvc.perform(post("/api/owner/franchises/{franchiseId}/coupons", franchiseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("매장 쿠폰 생성 실패 - 권한 없음")
    @WithMockUser(username = "notowner")
    void createStoreCoupon_notStoreOwner() throws Exception {
        // given
        Long storeId = 1L;
        LocalDateTime now = LocalDateTime.now();
        CouponCreateRequest request = CouponCreateRequest.builder()
                .name("3,000원 할인")
                .quantity(100)
                .couponType(CouponType.FIXED)
                .minPrice(15000)
                .discountValue(3000)
                .issueStartDate(now.plusDays(1))
                .issueEndDate(now.plusDays(7))
                .validDate(now.plusDays(30))
                .build();

        doThrow(new StoreException(StoreErrorCode.NOT_STORE_OWNER))
                .when(ownerCouponService).createStoreCoupon(eq(storeId), any(CouponCreateRequest.class), any(String.class));

        // when & then
        mockMvc.perform(post("/api/owner/stores/{storeId}/coupons", storeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }
}
