package com.idukbaduk.itseats.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.error.handler.GlobalExceptionHandler;
import com.idukbaduk.itseats.order.dto.OrderDetailResponse;
import com.idukbaduk.itseats.order.dto.OrderMenuItemDTO;
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
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OwnerOrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
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
}
