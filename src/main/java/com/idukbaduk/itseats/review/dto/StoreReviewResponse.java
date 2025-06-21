package com.idukbaduk.itseats.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreReviewResponse {
    private List<StoreReviewDto> reviews;
}
