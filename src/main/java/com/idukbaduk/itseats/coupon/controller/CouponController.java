package com.idukbaduk.itseats.coupon.controller;

import com.idukbaduk.itseats.coupon.dto.CouponIssueResponse;
import com.idukbaduk.itseats.coupon.dto.enums.CouponResponse;
import com.idukbaduk.itseats.coupon.service.CouponService;
import com.idukbaduk.itseats.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/coupons")
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/{couponId}/issue")
    public ResponseEntity<BaseResponse> issueCoupon(
            @PathVariable("couponId") Long couponId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        CouponIssueResponse response = couponService.issueCoupon(couponId, userDetails.getUsername());
        return BaseResponse.toResponseEntity(CouponResponse.ISSUE_COUPON_SUCCESS, response);
    }
}
