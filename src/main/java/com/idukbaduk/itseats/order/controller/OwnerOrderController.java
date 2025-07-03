package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.OwnerOrderService;
import jakarta.validation.Valid;
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

    @PostMapping("/orders/{orderId}/reject")
    public ResponseEntity<BaseResponse> rejectOrder(
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid OrderRejectRequest request) {
        OrderRejectResponse response = ownerOrderService.rejectOrder(orderId, request.getReason());
        return BaseResponse.toResponseEntity(OrderResponse.REJECT_ORDER_SUCCESS, response);
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<BaseResponse> acceptOrder(@PathVariable("orderId") Long orderId) {
        OrderAcceptResponse response = ownerOrderService.acceptOrder(orderId);
        return BaseResponse.toResponseEntity(OrderResponse.ACCEPT_ORDER_SUCCESS, response);
    }

    @PostMapping("/orders/{orderId}/ready")
    public ResponseEntity<BaseResponse> cookingComplete(@PathVariable("orderId") Long orderId) {
        OrderCookedResponse response = ownerOrderService.markAsCooked(orderId);
        return BaseResponse.toResponseEntity(OrderResponse.COOKED_SUCCESS, response);
    }

    @PostMapping("/orders/{orderId}/cooktime")
    public ResponseEntity<BaseResponse> setCookTime(
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid CookTimeRequest request
    ) {
        CookTimeResponse response = ownerOrderService.setCookTime(orderId, request.getCookTime());
        return BaseResponse.toResponseEntity(OrderResponse.SET_COOKTIME_SUCCESS, response);
    }
}
