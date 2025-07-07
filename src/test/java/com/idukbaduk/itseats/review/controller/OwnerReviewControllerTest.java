package com.idukbaduk.itseats.review.controller;

import com.idukbaduk.itseats.review.dto.ReviewDto;
import com.idukbaduk.itseats.review.dto.ReviewListResponse;
import com.idukbaduk.itseats.review.dto.enums.ReviewResponse;
import com.idukbaduk.itseats.review.service.OwnerReviewService;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OwnerReviewController.class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerReviewService ownerReviewService;

    @Test
    @DisplayName("가게 리뷰 조회 성공")
    @WithMockUser(username = "testowner")
    void getStoreReviews_success() throws Exception {
        // given
        Long storeId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);
        String username = "testowner";

        ReviewDto reviewDto = ReviewDto.builder()
                .reviewer("테스트리뷰어")
                .menuName("테스트메뉴")
                .orderNumber("ORDER123")
                .reviewImageUrl("http://test.com/image.jpg")
                .rating(5)
                .content("맛있어요!")
                .createdAt(LocalDateTime.now())
                .build();

        ReviewListResponse response = ReviewListResponse.builder()
                .startDate(startDate.toString())
                .endDate(endDate.toString())
                .reviews(List.of(reviewDto))
                .build();

        when(ownerReviewService.getReviewsByStoreAndPeriod(
                any(Long.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(String.class)))
                .thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/owner/{storeId}/reviews", storeId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(ReviewResponse.GET_STORE_REVIEWS_BY_PERIOD.getMessage()))
                .andExpect(jsonPath("$.data.reviews[0].reviewer").value("테스트리뷰어"));
    }

    @Test
    @DisplayName("가게 리뷰 조회 실패 - 가게 소유자가 아님")
    @WithMockUser(username = "testuser")
    void getStoreReviews_notStoreOwner() throws Exception {
        // given
        Long storeId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(ownerReviewService.getReviewsByStoreAndPeriod(
                any(Long.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(String.class)))
                .thenThrow(new StoreException(StoreErrorCode.NOT_STORE_OWNER));

        // when & then
        mockMvc.perform(get("/api/owner/{storeId}/reviews", storeId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("가게 리뷰 조회 실패 - 존재하지 않는 가게")
    @WithMockUser(username = "testowner")
    void getStoreReviews_storeNotFound() throws Exception {
        // given
        Long storeId = 999L;
        LocalDateTime startDate = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2024, 1, 31, 23, 59);

        when(ownerReviewService.getReviewsByStoreAndPeriod(
                any(Long.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                any(String.class)))
                .thenThrow(new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/owner/{storeId}/reviews", storeId)
                        .param("startDate", startDate.format(DateTimeFormatter.ISO_DATE_TIME))
                        .param("endDate", endDate.format(DateTimeFormatter.ISO_DATE_TIME)))
                .andExpect(status().isNotFound());
    }
}
