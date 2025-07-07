package com.idukbaduk.itseats.review.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.review.dto.StoreReviewResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{storeId}")
    public ResponseEntity<BaseResponse> getStoreReviews(@PathVariable Long storeId) {
        StoreReviewResponse response = reviewService.getReviewsByStore(storeId);
        return BaseResponse.toResponseEntity(ReviewResponse.GET_STORE_REVIEWS, response);
    }
}
