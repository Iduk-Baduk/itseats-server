package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.order.dto.AddressInfoDTO;
import com.idukbaduk.itseats.order.dto.RiderOrderDetailsResponse;
import com.idukbaduk.itseats.order.dto.OrderRequestResponse;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RiderOrderController.class)
class RiderOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RiderOrderService riderOrderService;

    @Test
    @DisplayName("주문 정보 조회 성공")
    @WithMockUser(username = "testuser")
    void getOrderDetails_success() throws Exception {
        // given
        Long orderId = 1L;
        RiderOrderDetailsResponse response = RiderOrderDetailsResponse.builder()
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
    void updateOrderStatusAccept_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(put("/api/rider/" + orderId + "/accept")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.UPDATE_STATUS_ACCEPT_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(RiderResponse.UPDATE_STATUS_ACCEPT_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("매장 도착 성공")
    @WithMockUser(username = "testuser")
    void updateOrderStatusArrived_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(put("/api/rider/" + orderId + "/arrived")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(RiderResponse.UPDATE_STATUS_ARRIVED_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(RiderResponse.UPDATE_STATUS_ARRIVED_SUCCESS.getMessage()));
    }

    @Test
    @DisplayName("배달 완료 성공")
    @WithMockUser(username = "testuser")
    void updateOrderStatusDone_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(put("/api/rider/" + orderId + "/done")
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
    void updateOrderStatusPickup_success() throws Exception {
        // given
        long orderId = 1L;

        // when & then
        mockMvc.perform(put("/api/rider/" + orderId + "/pickup")
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

    @Test
    @DisplayName("주문 요청 조회 성공")
    @WithMockUser(username = "testuser")
    void getOrderRequest_success() throws Exception {
        // given
        OrderRequestResponse mockResponse = OrderRequestResponse.builder()
                .orderId(1L)
                .deliveryType("DEFAULT")
                .storeName("스타벅스 구름점")
                .myLocation(AddressInfoDTO.builder().lat(37.5665).lng(126.9780).build())
                .storeLocation(AddressInfoDTO.builder().lat(37.5670).lng(126.9770).build())
                .deliveryFee(3000)
                .storeAddress("서울특별시 구름구 구름동100번길 10 1층")
                .validTime(LocalDateTime.now().plusMinutes(1))
                .build();

        when(riderOrderService.getOrderRequest("testuser")).thenReturn(mockResponse);

        // when & then
        mockMvc.perform(get("/api/rider/request")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(OrderResponse.GET_ORDER_REQUEST_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.httpStatus").value(OrderResponse.GET_ORDER_REQUEST_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.data.orderId").value(1))
                .andExpect(jsonPath("$.data.deliveryType").value("DEFAULT"))
                .andExpect(jsonPath("$.data.storeName").value("스타벅스 구름점"))
                .andExpect(jsonPath("$.data.myLocation.lat").value(37.5665))
                .andExpect(jsonPath("$.data.myLocation.lng").value(126.9780))
                .andExpect(jsonPath("$.data.storeLocation.lat").value(37.5670))
                .andExpect(jsonPath("$.data.storeLocation.lng").value(126.9770))
                .andExpect(jsonPath("$.data.deliveryFee").value(3000))
                .andExpect(jsonPath("$.data.storeAddress").value("서울특별시 구름구 구름동100번길 10 1층"));
    }
}
