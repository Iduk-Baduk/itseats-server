package com.idukbaduk.itseats.rider.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.NearByOrderRequest;
import com.idukbaduk.itseats.rider.dto.RejectReasonRequest;
import com.idukbaduk.itseats.rider.dto.enums.RiderResponse;
import com.idukbaduk.itseats.rider.service.RiderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/ready-order")
    public ResponseEntity<BaseResponse> getNearbyOrders(
            @RequestParam("latitude") @NotNull(message = "위도는 필수값입니다.")
            @DecimalMin(value = "-90.0", message = "위도는 -90도 이상이어야 합니다.")
            @DecimalMax(value = "90.0", message = "위도는 90도 이하여야 합니다.")
            Double latitude,

            @RequestParam("longitude") @NotNull(message = "경도는 필수값입니다.")
            @DecimalMin(value = "-180.0", message = "경도는 -180도 이상이어야 합니다.")
            @DecimalMax(value = "180.0", message = "경도는 180도 이하여야 합니다.")
            Double longitude
    ) {

        NearByOrderRequest request = new NearByOrderRequest(latitude, longitude);

        return BaseResponse.toResponseEntity(
                RiderResponse.GET_NEARBY_ORDERS_SUCCESS,
                riderService.findNearbyOrders(request)
        );
    }
}
