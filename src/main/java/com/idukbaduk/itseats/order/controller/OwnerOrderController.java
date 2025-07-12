package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.OwnerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerOrderController {

    private final OwnerOrderService ownerOrderService;

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<BaseResponse> getOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        OwnerOrderDetailsResponse response = ownerOrderService.getOrderDetail(userDetails.getUsername(), orderId);
        return BaseResponse.toResponseEntity(OrderResponse.GET_ORDER_DETAILS_SUCCESS, response);
    }

    @GetMapping("/{storeId}/orders")
    public ResponseEntity<BaseResponse> getOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("storeId") Long storeId) {
        List<OrderReceptionResponse> orders = ownerOrderService.getOrders(userDetails.getUsername(), storeId);
        return BaseResponse.toResponseEntity(OrderResponse.GET_STORE_ORDERS_SUCCESS, orders);
    }

    @PutMapping("/orders/{orderId}/reject")
    public ResponseEntity<BaseResponse> rejectOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid OrderRejectRequest request) {
        OrderRejectResponse response
                = ownerOrderService.rejectOrder(userDetails.getUsername(), orderId, request.getReason());
        return BaseResponse.toResponseEntity(OrderResponse.REJECT_ORDER_SUCCESS, response);
    }

    @PutMapping("/orders/{orderId}/accept")
    public ResponseEntity<BaseResponse> acceptOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        OrderAcceptResponse response = ownerOrderService.acceptOrder(userDetails.getUsername(), orderId);
        return BaseResponse.toResponseEntity(OrderResponse.ACCEPT_ORDER_SUCCESS, response);
    }

    @PutMapping("/orders/{orderId}/ready")
    public ResponseEntity<BaseResponse> cookingComplete(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        OrderCookedResponse response = ownerOrderService.markAsCooked(userDetails.getUsername(), orderId);
        return BaseResponse.toResponseEntity(OrderResponse.COOKED_SUCCESS, response);
    }

    @PutMapping("/orders/{orderId}/cooktime")
    public ResponseEntity<BaseResponse> setCookTime(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId,
            @RequestBody @Valid CookTimeRequest request
    ) {
        CookTimeResponse response
                = ownerOrderService.setCookTime(userDetails.getUsername(), orderId, request.getCookTime());
        return BaseResponse.toResponseEntity(OrderResponse.SET_COOKTIME_SUCCESS, response);
    }
}
