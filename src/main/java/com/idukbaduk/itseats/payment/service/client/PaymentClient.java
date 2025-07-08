package com.idukbaduk.itseats.payment.service.client;

import com.idukbaduk.itseats.payment.dto.PaymentClientResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private final RestClient restClient;

    public PaymentClientResponse confirmPayment(PaymentConfirmRequest confirmRequest) {
        try {
            PaymentClientResponse clientResponse =  restClient.post().uri("/confirm")
                    .body(confirmRequest)
                    .retrieve()
                    .body(PaymentClientResponse.class);
            validatePaymentStatus(Objects.requireNonNull(clientResponse).getStatus());
            return clientResponse;
        } catch (ResourceAccessException e) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
        }
    }

    private void validatePaymentStatus(String status) {
        if (!status.equals("DONE")) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_FAIL);
        }
    }
}
