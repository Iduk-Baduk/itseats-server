package com.idukbaduk.itseats.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.review.dto.ReviewCreateRequest;
import com.idukbaduk.itseats.review.dto.ReviewCreateResponse;
import com.idukbaduk.itseats.review.entity.enums.MenuLiked;
import com.idukbaduk.itseats.review.service.ReviewService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReviewController.class)
public class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReviewService reviewService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("리뷰 작성 성공")
    @WithMockUser(username = "user1")
    void createReview_success() throws Exception {
        // given
        Long orderId = 1L;
        ReviewCreateRequest request = ReviewCreateRequest.builder()
                .storeStar(5)
                .riderStar(5)
                .menuLiked(MenuLiked.GOOD)
                .content("맛있어요")
                .build();

        ReviewCreateResponse response = ReviewCreateResponse.builder()
                .storeStar(5)
                .riderStar(5)
                .menuLiked(MenuLiked.GOOD)
                .content("맛있어요")
                .createdAt(LocalDateTime.of(2025, 6, 11, 6, 55, 0))
                .build();

        given(reviewService.createReview(eq(orderId), eq("user1"), any(ReviewCreateRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/api/reviews/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value("리뷰 작성 성공"))
                .andExpect(jsonPath("$.data.storeStar").value(5))
                .andExpect(jsonPath("$.data.riderStar").value(5))
                .andExpect(jsonPath("$.data.menuLiked").value("GOOD"))
                .andExpect(jsonPath("$.data.content").value("맛있어요"))
                .andExpect(jsonPath("$.data.createdAt").value("2025-06-11T06:55:00"));
    }
}
