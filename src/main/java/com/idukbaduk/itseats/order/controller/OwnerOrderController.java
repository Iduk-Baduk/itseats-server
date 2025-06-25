package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.OrderReceptionResponse;
import com.idukbaduk.itseats.order.dto.OrderRejectRequest;
import com.idukbaduk.itseats.order.dto.OrderRejectResponse;
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

    @GetMapping("/{storeId}/orders")
    public ResponseEntity<BaseResponse> getOrders(@PathVariable("storeId") Long storeId) {
        List<OrderReceptionResponse> orders = ownerOrderService.getOrders(storeId);
        return BaseResponse.toResponseEntity(OrderResponse.GET_STORE_ORDERS_SUCCESS, orders);
    }

    @PostMapping("/orders/{orderId}/reject")
    public ResponseEntity<BaseResponse> rejectOrder(
            @PathVariable("orderId") Long orderId,
            @RequestBody OrderRejectRequest request) {
        OrderRejectResponse response = ownerOrderService.rejectOrder(orderId, request.getReason());
        return BaseResponse.toResponseEntity(OrderResponse.REJECT_ORDER_SUCCESS, response);
    }
}
