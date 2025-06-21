package com.idukbaduk.itseats.payment.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaymentCreateResponse {

    private Long paymentId;
}
