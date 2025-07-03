package com.idukbaduk.itseats.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.error.handler.GlobalExceptionHandler;
import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.service.OwnerOrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() throws Exception {
        OrderMenuItemDTO menu1 = OrderMenuItemDTO.builder()
                .menuId(11L)
                .menuName("아메리카노")
                .quantity(2)
                .menuPrice(4000)
                .options(List.of("샷추가", "샷추가", "사이즈업"))
                .build();

        OrderMenuItemDTO menu2 = OrderMenuItemDTO.builder()
                .menuId(12L)
                .menuName("에스프레소")
                .quantity(1)
                .menuPrice(1500)
                .options(List.of())
                .build();

        OrderDetailResponse response = OrderDetailResponse.builder()
                .orderId(3L)
                .orderNumber("GRMT0N")
                .memberName("구름톤")
                .orderStatus("WAITING")
                .orderTime("2025-05-05T00:00:00")
                .totalPrice(34000)
                .menuItems(List.of(menu1, menu2))
                .build();

        when(ownerOrderService.getOrderDetail(anyLong())).thenReturn(response);

        mockMvc.perform(get("/api/owner/orders/{orderId}", 3)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(OrderResponse.GET_ORDER_DETAILS_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderResponse.GET_ORDER_DETAILS_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.orderId").value(3))
                .andExpect(jsonPath("$.data.orderNumber").value("GRMT0N"))
                .andExpect(jsonPath("$.data.menuItems[0].menuName").value("아메리카노"));
    }

    @Test
    @DisplayName("주문이 없으면 404 반환")
    void getOrderDetail_notFound() throws Exception {
        when(ownerOrderService.getOrderDetail(anyLong()))
                .thenThrow(new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        mockMvc.perform(get("/api/owner/orders/{orderId}", 999)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(OrderErrorCode.ORDER_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()));
    }

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
    @DisplayName("주문 거절 시 주문이 존재하지 않으면 에러 응답")
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

    @Test
    @DisplayName("조리 완료 성공 응답")
    void markAsCooked_success() throws Exception {
        // given
        Long orderId = 1L;
        given(ownerOrderService.markAsCooked(orderId))
                .willReturn(new OrderCookedResponse(true));

        // when & then
        mockMvc.perform(post("/api/owner/orders/{orderId}/ready", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(OrderResponse.COOKED_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderResponse.COOKED_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.success").value(true));
    }

    @Test
    @DisplayName("조리 완료 상태 변경 시 주문이 존재하지 않으면 에러 응답")
    void markAsCooked_orderNotFound() throws Exception {
        // given
        Long orderId = 2L;
        given(ownerOrderService.markAsCooked(orderId))
                .willThrow(new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/owner/orders/{orderId}/ready", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(OrderErrorCode.ORDER_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()));
    }

    @Test
    @DisplayName("예상 조리 시간 설정 성공 응답")
    void setCookTime_success() throws Exception {
        // given
        Long orderId = 12L;
        int cookTime = 15;
        CookTimeRequest request = new CookTimeRequest().builder()
                .cookTime(cookTime)
                .build();
        CookTimeResponse response = CookTimeResponse.builder()
                .orderId(orderId)
                .deliveryEta("06.10 14:46")
                .build();

        given(ownerOrderService.setCookTime(orderId, cookTime)).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/owner/orders/{order_id}/cooktime", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value(OrderResponse.SET_COOKTIME_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderResponse.SET_COOKTIME_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.deliveryEta").value("06.10 14:46"));
    }

    @Test
    @DisplayName("주문이 없으면 404 에러 응답")
    void setCookTime_orderNotFound() throws Exception {
        // given
        Long orderId = 99L;
        int cookTime = 10;
        CookTimeRequest request = new CookTimeRequest().builder()
                .cookTime(cookTime)
                .build();

        given(ownerOrderService.setCookTime(orderId, cookTime))
                .willThrow(new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        // when & then
        mockMvc.perform(post("/api/owner/orders/{order_id}/cooktime", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(OrderErrorCode.ORDER_NOT_FOUND.getStatus().value()))
                .andExpect(jsonPath("$.message").value(OrderErrorCode.ORDER_NOT_FOUND.getMessage()));
    }
}
