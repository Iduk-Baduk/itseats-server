package com.idukbaduk.itseats.order.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.order.dto.enums.OrderResponse;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.service.RiderOrderService;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PutMapping("/{orderId}/accept")
    public ResponseEntity<BaseResponse> updateOrderStatusAccept(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.acceptDelivery(userDetails.getUsername(), orderId);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_ACCEPT_SUCCESS);
    }

    @PutMapping("/{orderId}/arrived")
    public ResponseEntity<BaseResponse> updateOrderStatusArrived(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.updateOrderStatus(userDetails.getUsername(), orderId, OrderStatus.ARRIVED);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_ARRIVED_SUCCESS);
    }

    @PutMapping("/{orderId}/pickup")
    public ResponseEntity<BaseResponse> updateOrderStatusPickup(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.updateOrderStatus(userDetails.getUsername(), orderId, OrderStatus.DELIVERING);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_PICKUP_SUCCESS);
    }

    @PostMapping(path = "/{orderId}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> uploadRiderImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId,
            @RequestParam("image") MultipartFile image) {
        return BaseResponse.toResponseEntity(
                OrderResponse.UPLOAD_RIDER_IMAGE_SUCCESS,
                riderOrderService.uploadRiderImage(userDetails.getUsername(), orderId, image)
        );
    }

    @PutMapping("/{orderId}/done")
    public ResponseEntity<BaseResponse> updateOrderStatusDone(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("orderId") Long orderId) {
        riderOrderService.updateOrderStatus(userDetails.getUsername(), orderId, OrderStatus.DELIVERED);
        return BaseResponse.toResponseEntity(RiderResponse.UPDATE_STATUS_DELIVERED_SUCCESS);
    }

    @GetMapping("/request")
    public ResponseEntity<BaseResponse> getOrderRequest(
            @AuthenticationPrincipal UserDetails userDetails) {
        return BaseResponse.toResponseEntity(
                OrderResponse.GET_ORDER_REQUEST_SUCCESS,
                riderOrderService.getOrderRequest(userDetails.getUsername())
        );
    }
}
