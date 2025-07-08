package com.idukbaduk.itseats.payment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {

    @NotNull(message = "토스 결제 키는 필수값입니다.")
    private String paymentKey;

    @NotNull(message = "토스 주문 아이디는 필수값입니다.")
    private String orderId;

    @NotNull(message = "총 금액은 필수값입니다.")
    @Positive(message = "총 금액은 양수값이어야 합니다.")
    private Long amount;
}
