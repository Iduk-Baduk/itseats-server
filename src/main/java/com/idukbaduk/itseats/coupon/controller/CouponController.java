package com.idukbaduk.itseats.coupon.controller;

import com.idukbaduk.itseats.coupon.dto.MyCouponListResponse;
import com.idukbaduk.itseats.coupon.dto.enums.CouponResponse;
import com.idukbaduk.itseats.coupon.service.CouponService;
import com.idukbaduk.itseats.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/coupons")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public ResponseEntity<BaseResponse> getMyCoupons(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MyCouponListResponse response = couponService.getMyCoupons(userDetails.getUsername());
        return BaseResponse.toResponseEntity(CouponResponse.GET_MY_COUPONS, response);
    }
}
