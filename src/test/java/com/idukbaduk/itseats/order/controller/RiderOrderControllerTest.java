package com.idukbaduk.itseats.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.order.dto.OrderDetailsResponse;
import com.idukbaduk.itseats.order.dto.RiderImageResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.RiderOrderService;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiderOrderController.class)
class RiderOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RiderOrderService riderOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 정보 조회 성공")
    @WithMockUser(username = "testuser")
    void getOrderDetails_success() throws Exception {
        // given
        Long orderId = 1L;
        OrderDetailsResponse response = OrderDetailsResponse.builder()
                .orderId(orderId)
                .build();

        when(riderOrderService.getOrderDetails(any(), any())).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/rider/" + orderId + "/details")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus")
                        .value(OrderResponse.GET_RIDER_ORDER_DETAILS_SUCCESS.getHttpStatus().value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(OrderResponse.GET_RIDER_ORDER_DETAILS_SUCCESS.getMessage()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.orderId").value(orderId));
    }

    @Test
    @DisplayName("배달 수락 성공")
    @WithMockUser(username = "testuser")
    void updateDeliveryStatusAccept_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(post("/api/rider/" + orderId + "/accept")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.UPDATE_STATUS_ACCEPT_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(RiderResponse.UPDATE_STATUS_ACCEPT_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("배달 완료 성공")
    @WithMockUser(username = "testuser")
    void updateDeliveryStatusDone_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(post("/api/rider/" + orderId + "/done")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.UPDATE_STATUS_DELIVERED_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(RiderResponse.UPDATE_STATUS_DELIVERED_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("픽업 완료 성공")
    @WithMockUser(username = "testuser")
    void updateDeliveryStatusPickup_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(post("/api/rider/" + orderId + "/pickup")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.UPDATE_STATUS_PICKUP_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(RiderResponse.UPDATE_STATUS_PICKUP_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("배달 완료 이미지 업로드 성공")
    @WithMockUser(username = "testuser")
    void uploadRiderImage_success() throws Exception {
        // given
        long orderId = 1L;
        MockMultipartFile image = new MockMultipartFile(
                "image", "test.jpg", "image/jpeg", "test".getBytes()
        );

        RiderImageResponse response = RiderImageResponse.builder()
                .image("https://example.com/test.jpg")
                .build();

        when(riderOrderService.uploadRiderImage(any(), any(), any(MultipartFile.class))).thenReturn(response);

        // when & then
        mockMvc.perform(multipart("/api/rider/{orderId}/picture", orderId)
                        .file(image)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(jsonPath("$.httpStatus")
                        .value(OrderResponse.UPLOAD_RIDER_IMAGE_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(OrderResponse.UPLOAD_RIDER_IMAGE_SUCCESS.getMessage()));
    }
}
