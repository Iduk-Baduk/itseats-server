package com.idukbaduk.itseats.review.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReportRequest {
    @NotBlank(message = "신고 사유는 필수입니다.")
    private String reason;
}
