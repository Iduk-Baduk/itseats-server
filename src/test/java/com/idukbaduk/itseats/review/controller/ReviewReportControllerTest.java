package com.idukbaduk.itseats.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.review.dto.ReviewReportRequest;
import com.idukbaduk.itseats.review.dto.ReviewReportResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.entity.enums.ReportStatus;
import com.idukbaduk.itseats.review.error.ReviewErrorCode;
import com.idukbaduk.itseats.review.error.ReviewException;
import com.idukbaduk.itseats.review.service.OwnerReviewService;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReviewReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerReviewService ownerReviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("리뷰 신고 성공")
    @WithMockUser(username = "owner1")
    void reportReview_success() throws Exception {
        // given
        Long storeId = 10L;
        Long reviewId = 100L;
        String reason = "욕설";
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason(reason)
                .build();

        ReviewReportResponse response = ReviewReportResponse.builder()
                .reporter("owner1")
                .reviewId(reviewId)
                .reason(reason)
                .reportedAt(LocalDateTime.of(2025, 7, 7, 15, 30))
                .reportStatus(ReportStatus.ACCEPTED)
                .build();

        given(ownerReviewService.reportReview(eq("owner1"), eq(storeId), eq(reviewId), any(ReviewReportRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/owner/{storeId}/reviews/{reviewId}/report", storeId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value(ReviewResponse.REPORT_REVIEW_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(ReviewResponse.REPORT_REVIEW_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.reporter").value("owner1"))
                .andExpect(jsonPath("$.data.reviewId").value(reviewId))
                .andExpect(jsonPath("$.data.reason").value(reason))
                .andExpect(jsonPath("$.data.reportStatus").value("ACCEPTED"));
    }

    @Test
    @DisplayName("가맹점주가 아니면 에러 발생")
    @WithMockUser(username = "owner1")
    void reportReview_notStoreOwner() throws Exception {
        // given
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        doThrow(new StoreException(StoreErrorCode.NOT_STORE_OWNER))
                .when(ownerReviewService).reportReview(eq("owner1"), eq(storeId), eq(reviewId), any(ReviewReportRequest.class));

        // when & then
        mockMvc.perform(post("/api/owner/{storeId}/reviews/{reviewId}/report", storeId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("리뷰가 없으면 에러 발생")
    @WithMockUser(username = "owner1")
    void reportReview_reviewNotFound() throws Exception {
        // given
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        doThrow(new ReviewException(ReviewErrorCode.REVIEW_NOT_FOUND))
                .when(ownerReviewService).reportReview(eq("owner1"), eq(storeId), eq(reviewId), any(ReviewReportRequest.class));

        // when & then
        mockMvc.perform(post("/api/owner/{storeId}/reviews/{reviewId}/report", storeId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("중복 신고 시 에러 발생")
    @WithMockUser(username = "owner1")
    void reportReview_alreadyReported() throws Exception {
        // given
        Long storeId = 10L;
        Long reviewId = 100L;
        ReviewReportRequest request = ReviewReportRequest.builder()
                .reason("욕설")
                .build();

        doThrow(new ReviewException(ReviewErrorCode.ALREADY_REPORTED))
                .when(ownerReviewService).reportReview(eq("owner1"), eq(storeId), eq(reviewId), any(ReviewReportRequest.class));

        // when & then
        mockMvc.perform(post("/api/owner/{storeId}/reviews/{reviewId}/report", storeId, reviewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
