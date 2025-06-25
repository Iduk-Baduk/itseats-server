package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderDetailResponse;
import com.idukbaduk.itseats.order.dto.OrderAcceptResponse;
import com.idukbaduk.itseats.order.dto.OrderReceptionResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.OwnerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerOrderController {

    private final OwnerOrderService ownerOrderService;

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<BaseResponse> getOrderDetail(@PathVariable("orderId") Long orderId) {
        OrderDetailResponse response = ownerOrderService.getOrderDetail(orderId);
        return BaseResponse.toResponseEntity(OrderResponse.GET_ORDER_DETAILS_SUCCESS, response);
    }

    @GetMapping("/{storeId}/orders")
    public ResponseEntity<BaseResponse> getOrders(@PathVariable("storeId") Long storeId) {
        List<OrderReceptionResponse> orders = ownerOrderService.getOrders(storeId);
        return BaseResponse.toResponseEntity(OrderResponse.GET_STORE_ORDERS_SUCCESS, orders);
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<BaseResponse> acceptOrder(@PathVariable("orderId") Long orderId) {
        OrderAcceptResponse response = ownerOrderService.acceptOrder(orderId);
        return BaseResponse.toResponseEntity(OrderResponse.ACCEPT_ORDER_SUCCESS, response);
    }
}
