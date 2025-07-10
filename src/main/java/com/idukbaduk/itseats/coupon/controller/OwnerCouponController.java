package com.idukbaduk.itseats.coupon.controller;

import com.idukbaduk.itseats.coupon.dto.CouponCreateRequest;
import com.idukbaduk.itseats.coupon.dto.FranchiseCouponCreateResponse;
import com.idukbaduk.itseats.coupon.dto.StoreCouponCreateResponse;
import com.idukbaduk.itseats.coupon.dto.enums.CouponResponse;
import com.idukbaduk.itseats.coupon.service.OwnerCouponService;
import com.idukbaduk.itseats.global.response.BaseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerCouponController {

    private final OwnerCouponService ownerCouponService;

    @PostMapping("/stores/{storeId}/coupons")
    public ResponseEntity<BaseResponse> createStoreCoupon(
            @PathVariable("storeId") Long storeId,
            @RequestBody @Valid CouponCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        StoreCouponCreateResponse response =
                ownerCouponService.createStoreCoupon(storeId, request, userDetails.getUsername());
        return BaseResponse.toResponseEntity(CouponResponse.CREATE_COUPON_SUCCESS, response);
    }

    @PostMapping("franchises/{franchiseId}/coupons")
    public ResponseEntity<BaseResponse> createFranchiseCoupon(
            @PathVariable("franchiseId") Long franchiseId,
            @RequestBody @Valid CouponCreateRequest request
    ) {
        FranchiseCouponCreateResponse response = ownerCouponService.createFranchiseCoupon(franchiseId, request);
        return BaseResponse.toResponseEntity(CouponResponse.CREATE_COUPON_SUCCESS, response);
    }
}
