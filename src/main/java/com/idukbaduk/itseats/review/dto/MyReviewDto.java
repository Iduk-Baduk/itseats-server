package com.idukbaduk.itseats.review.dto;

import com.idukbaduk.itseats.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MyReviewDto {
    private Long reviewId;
    private String storeName;
    private int rating;
    private String content;
    private LocalDateTime createdAt;

    public static MyReviewDto of(Review review) {
        return MyReviewDto.builder()
                .storeName(review.getStore().getStoreName())
                .rating(review.getStoreStar())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .reviewId(review.getReviewId())
                .build();
    }
}
