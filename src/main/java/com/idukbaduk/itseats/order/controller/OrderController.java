package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderNewRequest;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/new")
    public ResponseEntity<BaseResponse> getOrderNew(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody OrderNewRequest orderNewRequest) {
        return BaseResponse.toResponseEntity(
                OrderResponse.GET_ORDER_DETAILS_SUCCESS,
                orderService.getOrderNew(userDetails.getUsername(), orderNewRequest)
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
