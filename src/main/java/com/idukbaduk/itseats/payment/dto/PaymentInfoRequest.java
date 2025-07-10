package com.idukbaduk.itseats.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentInfoRequest {

    private Long orderId;
    private List<Long> coupons;
    private Long totalCost;
    private String paymentMethod;
    private String storeRequest;
    private String riderRequest;
}
