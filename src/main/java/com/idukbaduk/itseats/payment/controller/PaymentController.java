package com.idukbaduk.itseats.payment.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.dto.PaymentCreateResponse;
import com.idukbaduk.itseats.payment.dto.enums.PaymentResponse;
import com.idukbaduk.itseats.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
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

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<BaseResponse> confirmPayment(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long paymentId,
            @RequestBody @Valid PaymentConfirmRequest paymentConfirmRequest) {
        
        // 디버깅을 위한 로그 추가
        System.out.println("=== 결제 확인 요청 ===");
        System.out.println("사용자: " + userDetails.getUsername());
        System.out.println("결제 ID: " + paymentId);
        System.out.println("요청 데이터: " + paymentConfirmRequest);
        
        paymentService.confirmPayment(userDetails.getUsername(), paymentId, paymentConfirmRequest);
        return BaseResponse.toResponseEntity(PaymentResponse.CONFIRM_PAYMENT_SUCCESS);
    }

    // 테스트용 임시 결제 데이터 생성 API
    @PostMapping("/test/create")
    public ResponseEntity<BaseResponse> createTestPayment(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        System.out.println("=== 테스트 결제 생성 ===");
        System.out.println("사용자: " + userDetails.getUsername());
        
        // 임시 결제 데이터 생성
        PaymentInfoRequest testRequest = PaymentInfoRequest.builder()
                .orderId(1L)  // 임시 주문 ID
                .totalCost(15000L)
                .paymentMethod("TOSS_PAYMENTS")
                .storeRequest("테스트 요청")
                .riderRequest("테스트 라이더 요청")
                .build();
        
        PaymentCreateResponse response = paymentService.createPayment(userDetails.getUsername(), testRequest);
        
        System.out.println("생성된 결제 ID: " + response.getPaymentId());
        
        return BaseResponse.toResponseEntity(
                PaymentResponse.CREATE_PAYMENT_SUCCESS,
                response
        );
    }
}
