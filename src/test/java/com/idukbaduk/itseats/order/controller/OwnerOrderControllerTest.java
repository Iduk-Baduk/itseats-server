package com.idukbaduk.itseats.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderReceptionResponse;
import com.idukbaduk.itseats.order.dto.OrderRejectRequest;
import com.idukbaduk.itseats.order.dto.OrderRejectResponse;
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
    @DisplayName("주문 거절 성공 응답")
    void rejectOrder_success() throws Exception {
        // given: 거절 사유를 포함한 요청과 서비스의 정상 동작
        Long orderId = 1L;
        String reason = "재고 부족";
        OrderRejectRequest request = new OrderRejectRequest(reason);

        given(ownerOrderService.rejectOrder(orderId, reason))
                .willReturn(new OrderRejectResponse(true, reason));

        // when & then: API 호출 결과 정상 응답 검증
        mockMvc.perform(post("/api/owner/orders/{orderId}/reject", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(OrderResponse.REJECT_ORDER_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderResponse.REJECT_ORDER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.success").value(true))
                .andExpect(jsonPath("$.data.reason").value(reason));
    }

    @Test
    @DisplayName("주문 거절 시주문이 존재하지 않으면 에러 응답")
    void rejectOrder_orderNotFound() throws Exception {
        // given
        Long orderId = 2L;
        String reason = "재고 부족";
        OrderRejectRequest request = new OrderRejectRequest(reason);

        given(ownerOrderService.rejectOrder(orderId, reason))
                .willThrow(new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/owner/orders/{orderId}/reject", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(OrderErrorCode.ORDER_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()));
    }
}
