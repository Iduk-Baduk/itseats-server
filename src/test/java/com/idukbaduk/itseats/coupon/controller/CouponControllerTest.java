package com.idukbaduk.itseats.coupon.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.coupon.dto.CouponIssueResponse;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.service.CouponService;
import com.idukbaduk.itseats.coupon.dto.enums.CouponResponse;
import com.idukbaduk.itseats.global.response.BaseResponse;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
@AutoConfigureMockMvc(addFilters = false)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("쿠폰 발급 성공")
    @WithMockUser(username = "user1")
    void issueCoupon_success() throws Exception {
        // given
        Long couponId = 10L;
        CouponIssueResponse response = CouponIssueResponse.builder()
                .memberCouponId(101L)
                .couponId(10L)
                .name("3,000원 할인")
                .discountValue(3000)
                .minPrice(15000)
                .issueDate(LocalDateTime.of(2025, 7, 8, 0, 0))
                .validDate(LocalDateTime.of(2025, 7, 31, 23, 59))
                .isUsed(false)
                .build();

        given(couponService.issueCoupon(eq(couponId), eq("user1"))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/coupons/{couponId}/issue", couponId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value(CouponResponse.ISSUE_COUPON_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(CouponResponse.ISSUE_COUPON_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.couponId").value(10))
                .andExpect(jsonPath("$.data.name").value("3,000원 할인"))
                .andExpect(jsonPath("$.data.discountValue").value(3000))
                .andExpect(jsonPath("$.data.minPrice").value(15000))
                .andExpect(jsonPath("$.data.used").value(false));
    }

    @Test
    @DisplayName("이미 발급받은 쿠폰이면 409 반환")
    @WithMockUser(username = "user1")
    void issueCoupon_alreadyIssued() throws Exception {
        // given
        Long couponId = 10L;
        doThrow(new com.idukbaduk.itseats.coupon.error.CouponException(CouponErrorCode.ALREADY_ISSUED))
                .when(couponService).issueCoupon(eq(couponId), eq("user1"));

        // when & then
        mockMvc.perform(post("/api/coupons/{couponId}/issue", couponId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("발급 수량 초과 시 409 반환")
    @WithMockUser(username = "user1")
    void issueCoupon_quantityExceeded() throws Exception {
        // given
        Long couponId = 10L;
        doThrow(new com.idukbaduk.itseats.coupon.error.CouponException(CouponErrorCode.QUANTITY_EXCEEDED))
                .when(couponService).issueCoupon(eq(couponId), eq("user1"));

        // when & then
        mockMvc.perform(post("/api/coupons/{couponId}/issue", couponId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("유효기간 외 발급 시 400 반환")
    @WithMockUser(username = "user1")
    void issueCoupon_invalidPeriod() throws Exception {
        // given
        Long couponId = 10L;
        doThrow(new com.idukbaduk.itseats.coupon.error.CouponException(CouponErrorCode.INVALID_PERIOD))
                .when(couponService).issueCoupon(eq(couponId), eq("user1"));

        // when & then
        mockMvc.perform(post("/api/coupons/{couponId}/issue", couponId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
