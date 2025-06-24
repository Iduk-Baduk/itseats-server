package com.idukbaduk.itseats.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.order.dto.OrderDetailsResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.RiderOrderService;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(response)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.httpStatus")
                        .value(OrderResponse.GET_RIDER_ORDER_DETAILS_SUCCESS.getHttpStatus().value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value(OrderResponse.GET_RIDER_ORDER_DETAILS_SUCCESS.getMessage()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.orderId").value(orderId));
    }
}