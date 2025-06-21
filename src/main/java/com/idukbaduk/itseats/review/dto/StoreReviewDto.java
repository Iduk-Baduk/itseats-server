package com.idukbaduk.itseats.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreReviewDto {
    private String imageUrl;
    private String reviewer;
    private double rating;
    private String content;
    private String createdAt;
}
