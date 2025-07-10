package com.idukbaduk.itseats.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentClientResponse {

    private String paymentKey;
    private String orderId;
    private Long totalAmount;
    private String status;
}
