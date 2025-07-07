package com.idukbaduk.itseats.review.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.review.dto.ReviewReportRequest;
import com.idukbaduk.itseats.review.dto.ReviewReportResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.service.OwnerReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerReviewController {

    private final OwnerReviewService ownerReviewService;

    @PostMapping("/{storeId}/reviews/{reviewId}/report")
    public ResponseEntity<BaseResponse> reportReview(
            @PathVariable("storeId") Long storeId,
            @PathVariable("reviewId") Long reviewId,
            @RequestBody ReviewReportRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReviewReportResponse response =
                ownerReviewService.reportReview(userDetails.getUsername(), storeId, reviewId, request);

        return BaseResponse.toResponseEntity(ReviewResponse.REPORT_REVIEW_SUCCESS, response);
    }
}
