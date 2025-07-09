package com.idukbaduk.itseats.coupon.controller;

import com.idukbaduk.itseats.coupon.dto.MyCouponDto;
import com.idukbaduk.itseats.coupon.dto.MyCouponListResponse;
import com.idukbaduk.itseats.coupon.dto.enums.CouponResponse;
import com.idukbaduk.itseats.coupon.service.CouponService;
import com.idukbaduk.itseats.global.response.BaseResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CouponController.class)
class CouponControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CouponService couponService;

    @Test
    @DisplayName("내 쿠폰 목록 조회 API 성공")
    @WithMockUser(username = "testuser")
    void getMyCoupons_success() throws Exception {
        // given
        MyCouponDto dto = MyCouponDto.builder()
                .couponType(com.idukbaduk.itseats.coupon.entity.enums.CouponType.FIXED)
                .minPrice(10000)
                .discountValue(1000)
                .issueDate(LocalDateTime.now())
                .validDate(LocalDateTime.now().plusDays(1))
                .canUsed(true)
                .build();
        MyCouponListResponse listResponse = MyCouponListResponse.builder()
                .myCouponDtos(List.of(dto))
                .build();

        when(couponService.getMyCoupons(anyString())).thenReturn(listResponse);

        // when & then
        mockMvc.perform(get("/api/coupons")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.myCouponDtos[0].couponType").value("FIXED"))
                .andExpect(jsonPath("$.data.myCouponDtos[0].canUsed").value(true));
    }
}
