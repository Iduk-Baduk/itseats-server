package com.idukbaduk.itseats.review.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.review.dto.ReviewCreateRequest;
import com.idukbaduk.itseats.review.dto.ReviewCreateResponse;
import com.idukbaduk.itseats.review.dto.StoreReviewResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{storeId}")
    public ResponseEntity<BaseResponse> getStoreReviews(@PathVariable Long storeId) {
        StoreReviewResponse response = reviewService.getReviewsByStore(storeId);
        return BaseResponse.toResponseEntity(ReviewResponse.GET_STORE_REVIEW_SUCCESS, response);
    }

    @PostMapping("/{orderId}")
    public ResponseEntity<BaseResponse> createReview(
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid ReviewCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        ReviewCreateResponse response = reviewService.createReview(orderId, userDetails.getUsername(), request);
        return BaseResponse.toResponseEntity(ReviewResponse.CREATE_REVIEW_SUCCESS, response);
    }
}
