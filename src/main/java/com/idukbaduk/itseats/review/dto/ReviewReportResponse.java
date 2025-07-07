package com.idukbaduk.itseats.review.dto;

import com.idukbaduk.itseats.review.entity.enums.ReportStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ReviewReportResponse {
    private String reporter;
    private Long reviewId;
    private String reason;
    private LocalDateTime reportedAt;
    private ReportStatus reportStatus;
}
