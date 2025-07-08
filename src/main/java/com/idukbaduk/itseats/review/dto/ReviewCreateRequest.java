package com.idukbaduk.itseats.review.dto;

import com.idukbaduk.itseats.review.entity.enums.MenuLiked;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateRequest {
    @Min(1) @Max(5)
    private int storeStar;

    @Min(1) @Max(5)
    private int riderStar;

    @NotNull
    private MenuLiked menuLiked;

    @NotBlank
    private String content;
}
