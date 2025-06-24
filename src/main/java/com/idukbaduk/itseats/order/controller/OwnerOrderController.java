package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderDetailResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.service.OwnerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
