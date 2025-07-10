package com.idukbaduk.itseats.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewListResponse {
    private String startDate;
    private String endDate;
    private List<ReviewDto> reviews;
}
