package com.idukbaduk.itseats.payment.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.dto.PaymentCreateResponse;
import com.idukbaduk.itseats.payment.dto.enums.PaymentResponse;
import com.idukbaduk.itseats.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.idukbaduk.itseats.payment.dto.TossPaymentConfirmRequest;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<BaseResponse> createPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PaymentInfoRequest paymentInfoRequest) {
        if (userDetails == null) {
            log.error("결제 생성 실패 - 인증 정보 없음");
            return BaseResponse.toResponseEntity(
                HttpStatus.UNAUTHORIZED, 
                "인증 정보가 없습니다. 다시 로그인해주세요."
            );
        }
        log.info("결제 생성 요청 - username: {}, orderId: {}", userDetails.getUsername(), paymentInfoRequest.getOrderId());
        try {
            PaymentCreateResponse response = paymentService.createPayment(userDetails.getUsername(), paymentInfoRequest);
            log.info("결제 생성 성공 - paymentId: {}", response.getPaymentId());
            return BaseResponse.toResponseEntity(
                    PaymentResponse.CREATE_PAYMENT_SUCCESS,
                    response
            );
        } catch (Exception e) {
            log.error("결제 생성 실패 - username: {}, orderId: {}, error: {}", userDetails.getUsername(), paymentInfoRequest.getOrderId(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<BaseResponse> confirmPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long paymentId,
            @RequestBody PaymentConfirmRequest paymentConfirmRequest) {
        if (userDetails == null) {
            log.error("결제 확인 실패 - 인증 정보 없음");
            return BaseResponse.toResponseEntity(
                HttpStatus.UNAUTHORIZED, 
                "인증 정보가 없습니다. 다시 로그인해주세요."
            );
        }
        log.info("결제 확인 요청 - username: {}, paymentId: {}", userDetails.getUsername(), paymentId);
        try {
            paymentService.confirmPayment(userDetails.getUsername(), paymentId, paymentConfirmRequest);
            log.info("결제 확인 성공 - paymentId: {}", paymentId);
            return BaseResponse.toResponseEntity(PaymentResponse.CONFIRM_PAYMENT_SUCCESS);
        } catch (Exception e) {
            log.error("결제 확인 실패 - username: {}, paymentId: {}, error: {}", userDetails.getUsername(), paymentId, e.getMessage());
            throw e;
        }
    }

    /**
     * 토스페이먼츠 결제 확인 (단순화된 1단계 플로우)
     */
    @PostMapping("/confirm")
    public ResponseEntity<BaseResponse> confirmTossPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody TossPaymentConfirmRequest request) {
        if (userDetails == null) {
            log.error("토스 결제 확인 실패 - 인증 정보 없음");
            return BaseResponse.toResponseEntity(
                HttpStatus.UNAUTHORIZED, 
                "인증 정보가 없습니다. 다시 로그인해주세요."
            );
        }
        log.info("토스 결제 확인 요청 - username: {}, orderId: {}", userDetails.getUsername(), request.getOrderId());
        try {
            paymentService.confirmTossPayment(userDetails.getUsername(), request);
            log.info("토스 결제 확인 성공 - orderId: {}", request.getOrderId());
            return BaseResponse.toResponseEntity(PaymentResponse.TOSS_PAYMENT_CONFIRM_SUCCESS);
        } catch (Exception e) {
            log.error("토스 결제 확인 실패 - username: {}, orderId: {}, error: {}", userDetails.getUsername(), request.getOrderId(), e.getMessage());
            throw e;
        }
    }
}
