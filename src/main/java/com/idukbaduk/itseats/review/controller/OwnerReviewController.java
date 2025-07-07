package com.idukbaduk.itseats.review.controller;

import com.idukbaduk.itseats.auths.dto.CustomMemberDetails;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.review.dto.ReviewListResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.service.OwnerReviewService;
import com.idukbaduk.itseats.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerReviewController {

    private final OwnerReviewService ownerReviewService;

    @GetMapping("/{storeId}/reviews")
    public ResponseEntity<BaseResponse> getStoreReviews(
            @PathVariable("storeId") Long storeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        ReviewListResponse response =
                ownerReviewService.getReviewsByStoreAndPeriod(storeId, startDate, endDate, userDetails.getUsername());
        return BaseResponse.toResponseEntity(ReviewResponse.GET_STORE_REVIEWS_BY_PERIOD, response);
    }
}
