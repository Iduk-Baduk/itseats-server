package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
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

    @GetMapping("/{store_id}/orders")
    public ResponseEntity<BaseResponse> getOrders(@PathVariable("store_id") Long storeId) {
        List<OrderReceptionResponse> orders = ownerOrderService.getOrders(storeId);
        return BaseResponse.toResponseEntity(OrderResponse.GET_STORE_ORDERS_SUCCESS, orders);
    }
}
