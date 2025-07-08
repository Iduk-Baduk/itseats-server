package com.idukbaduk.itseats.review.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.review.dto.ReviewReportRequest;
import com.idukbaduk.itseats.review.dto.ReviewReportResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.service.OwnerReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.idukbaduk.itseats.review.dto.ReviewListResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
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

    @PostMapping("/{storeId}/reviews/{reviewId}/report")
    public ResponseEntity<BaseResponse> reportReview(
            @PathVariable("storeId") Long storeId,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody @Valid ReviewReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReviewReportResponse response =
                ownerReviewService.reportReview(userDetails.getUsername(), storeId, reviewId, request);

        return BaseResponse.toResponseEntity(ReviewResponse.REPORT_REVIEW_SUCCESS, response);
    }
  
    @GetMapping("/{storeId}/reviews")
    public ResponseEntity<BaseResponse> getStoreReviews(
            @PathVariable("storeId") Long storeId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("시작 날짜는 종료 날짜보다 이전이어야 합니다.");
        }
        ReviewListResponse response =
                ownerReviewService.getReviewsByStoreAndPeriod(storeId, startDate, endDate, userDetails.getUsername());
        return BaseResponse.toResponseEntity(ReviewResponse.GET_STORE_REVIEWS_BY_PERIOD, response);
    }
}
