package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderCreateRequest;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.OrderService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/{storeId}/details")
    public ResponseEntity<BaseResponse> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("storeId") Long storeId,
            @RequestParam("coupon") @Nullable Long couponId,
            @RequestParam("orderPrice") @Nullable Integer orderPrice) {
        return BaseResponse.toResponseEntity(
                OrderResponse.GET_ORDER_DETAILS_SUCCESS,
                orderService.getOrderDetails(userDetails.getUsername(), storeId, couponId, orderPrice)
        );
    }

    @PostMapping("/new")
    public ResponseEntity<BaseResponse> createOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderCreateRequest orderCreateRequest) {
        return BaseResponse.toResponseEntity(
                OrderResponse.CREATE_ORDER_SUCCESS,
                orderService.createOrder(userDetails.getUsername(), orderCreateRequest)
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "") String keyword,
            @PageableDefault(page = 0, size = 20) Pageable pageable
    ) {
        return BaseResponse.toResponseEntity(
                OrderResponse.GET_ORDERS_SUCCESS,
                orderService.getOrders(userDetails.getUsername(), keyword, pageable)
        );
    }

    @GetMapping("/{orderId}/status")
    public ResponseEntity<BaseResponse> getOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        return BaseResponse.toResponseEntity(
                OrderResponse.GET_ORDER_STATUS_SUCCESS,
                orderService.getOrderStatus(userDetails.getUsername(), orderId)
        );
    }
}
