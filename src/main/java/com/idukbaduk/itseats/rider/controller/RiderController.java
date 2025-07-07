package com.idukbaduk.itseats.rider.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.NearByOrderRequest;
import com.idukbaduk.itseats.rider.dto.RejectReasonRequest;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import com.idukbaduk.itseats.rider.service.RiderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rider")
@RequiredArgsConstructor
public class RiderController {

    private final RiderService riderService;

    @PostMapping("/working")
    public ResponseEntity<BaseResponse> modifyWorking(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid ModifyWorkingRequest modifyWorkingRequest) {
        return BaseResponse.toResponseEntity(
                RiderResponse.MODIFY_IS_WORKING_SUCCESS,
                riderService.modifyWorking(userDetails.getUsername(), modifyWorkingRequest)
        );
    }

    @PutMapping("/{orderId}/reject")
    public ResponseEntity<BaseResponse> rejectDelivery(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            @RequestBody @Valid RejectReasonRequest reasonRequest) {
        return BaseResponse.toResponseEntity(
                RiderResponse.REJECT_DELIVERY_SUCCESS,
                riderService.rejectDelivery(userDetails.getUsername(), orderId, reasonRequest)
        );
    }

    @PostMapping("/ready-order")
    public ResponseEntity<BaseResponse> getNearbyOrders(
            @RequestBody NearByOrderRequest request
    ) {

        return BaseResponse.toResponseEntity(
                RiderResponse.GET_NEARBY_ORDERS_SUCCESS,
                riderService.findNearbyOrders(request)
        );
    }
}
