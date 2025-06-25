package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.service.RiderOrderService;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderOrderController {

    private final RiderOrderService riderOrderService;

    @GetMapping("/{orderId}/details")
    public ResponseEntity<BaseResponse> getOrderDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        return BaseResponse.toResponseEntity(
                OrderResponse.GET_RIDER_ORDER_DETAILS_SUCCESS,
                riderOrderService.getOrderDetails(userDetails.getUsername(), orderId)
        );
    }

    @PostMapping("/{orderId}/accept")
    public ResponseEntity<BaseResponse> updateDeliveryStatusAccept(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.acceptDelivery(userDetails.getUsername(), orderId);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_ACCEPT_SUCCESS);
    }

    @PostMapping("/{orderId}/pickup")
    public ResponseEntity<BaseResponse> updateDeliveryStatusPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.updateOrderStatusAfterAccept(userDetails.getUsername(), orderId, OrderStatus.DELIVERING);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_PICKUP_SUCCESS);
    }

    @PostMapping("/{orderId}/done")
    public ResponseEntity<BaseResponse> updateDeliveryStatusDone(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.updateOrderStatusAfterAccept(userDetails.getUsername(), orderId, OrderStatus.DELIVERED);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_DELIVERED_SUCCESS);
    }
}
