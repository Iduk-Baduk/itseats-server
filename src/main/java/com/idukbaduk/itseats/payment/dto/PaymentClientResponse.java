package com.idukbaduk.itseats.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentClientResponse {

    private String tossPaymentKey;
    private String tossOrderId;
    private Long totalAmount;
    private String status;
}
