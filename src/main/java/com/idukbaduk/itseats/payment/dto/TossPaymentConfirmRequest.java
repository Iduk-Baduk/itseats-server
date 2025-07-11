package com.idukbaduk.itseats.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TossPaymentConfirmRequest {
    
    private String paymentKey;      // 토스페이먼츠 결제키
    private String orderId;         // 토스페이먼츠 주문ID
    private Long amount;            // 결제 금액
    private Long orderIdForDb;      // DB의 주문 ID (Long 타입)
    private String storeRequest;    // 매장 요청사항
    private String riderRequest;    // 라이더 요청사항
    private Long memberCouponId;    // 사용할 쿠폰 ID (선택사항)
} 
