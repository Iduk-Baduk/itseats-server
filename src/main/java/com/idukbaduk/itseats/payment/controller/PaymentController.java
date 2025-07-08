package com.idukbaduk.itseats.payment.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.dto.enums.PaymentResponse;
import com.idukbaduk.itseats.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<BaseResponse> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentInfoRequest paymentInfoRequest) {
        return BaseResponse.toResponseEntity(
                PaymentResponse.CREATE_PAYMENT_SUCCESS,
                paymentService.createPayment(userDetails.getUsername(), paymentInfoRequest)
        );
    }

    @PostMapping("/confirm")
    public ResponseEntity<BaseResponse> confirmPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentConfirmRequest paymentConfirmRequest) {
        return BaseResponse.toResponseEntity(
                PaymentResponse.CONFIRM_PAYMENT_SUCCESS,
                paymentService.confirmPayment(userDetails.getUsername(), paymentConfirmRequest)
        );
    }
}
