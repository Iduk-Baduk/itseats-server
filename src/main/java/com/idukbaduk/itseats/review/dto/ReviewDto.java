package com.idukbaduk.itseats.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDto {
    private String reviewer;
    private String menuName;
    private String orderNumber;
    private String reviewImageUrl;
    private int rating;
    private String content;
    private LocalDateTime createdAt;
    private Long reviewId;
}
