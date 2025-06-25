package com.idukbaduk.itseats.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderAcceptResponse;
import com.idukbaduk.itseats.order.dto.OrderReceptionResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.service.OwnerOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OwnerOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OwnerOrderService ownerOrderService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("주문 접수 정보 조회 성공")
    void getOrders_success() throws Exception {
        OrderReceptionResponse response = OrderReceptionResponse.builder()
                .orderNumber("ORD123")
                .orderTime("2025-06-24T12:00:00")
                .menuCount(2)
                .totalPrice(8000)
                .menuItems(List.of())
                .orderStatus("COMPLETED")
                .customerRequest("빨리 주세요 조심히 운전")
                .riderPhone("010-1234-5678")
                .build();

        when(ownerOrderService.getOrders(anyLong())).thenReturn(List.of(response));

        mockMvc.perform(get("/api/owner/1/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(OrderResponse.GET_STORE_ORDERS_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderResponse.GET_STORE_ORDERS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data[0].orderNumber").value("ORD123"))
                .andExpect(jsonPath("$.data[0].customerRequest").value("빨리 주세요 조심히 운전"));
    }

    @Test
    @DisplayName("주문이 없을 때 빈 리스트 반환")
    void getOrders_empty() throws Exception {
        when(ownerOrderService.getOrders(anyLong())).thenReturn(List.of());

        mockMvc.perform(get("/api/owner/1/orders")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("주문 수락 성공")
    void acceptOrder_success() throws Exception {
        // given
        Long orderId = 1L;
        given(ownerOrderService.acceptOrder(orderId)).willReturn(new OrderAcceptResponse(true));

        // when & then


        mockMvc.perform(post("/api/owner/orders/" + orderId + "/accept")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus")
                        .value(OrderResponse.ACCEPT_ORDER_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message")
                        .value(OrderResponse.ACCEPT_ORDER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.success")
                        .value(true));
    }

    @Test
    @DisplayName("주문 수락 시 주문이 존재하지 않는 경우 에러 발생")
    void acceptOrder_orderNotFound() throws Exception {
        // given
        Long orderId = 2L;
        given(ownerOrderService.acceptOrder(orderId))
                .willThrow(new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/owner/orders/{orderId}/accept", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(OrderErrorCode.ORDER_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()));
    }
}
