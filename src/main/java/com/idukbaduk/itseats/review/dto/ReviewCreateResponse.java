package com.idukbaduk.itseats.review.dto;

import com.idukbaduk.itseats.review.entity.enums.MenuLiked;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewCreateResponse {
    private int storeStar;
    private int riderStar;
    private MenuLiked menuLiked;
    private String content;
    private LocalDateTime createdAt;
}
