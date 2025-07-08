package com.idukbaduk.itseats.payment.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.payment.dto.PaymentClientResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentConfirmErrorCode;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class PaymentClient {

    private static final String PAYMENT_SUCCESS_STATUS = "DONE";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public PaymentClientResponse confirmPayment(PaymentConfirmRequest confirmRequest) {
        try {
            PaymentClientResponse clientResponse =  restClient.post().uri("/confirm")
                    .body(confirmRequest)
                    .retrieve()
                    .onStatus(httpStatus -> httpStatus.is4xxClientError() || httpStatus.is5xxServerError(),
                            (request, response) -> handlePaymentError(response))
                    .body(PaymentClientResponse.class);
            validatePaymentStatus(Objects.requireNonNull(clientResponse).getStatus());
            return clientResponse;
        } catch (ResourceAccessException e) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
        }
    }

    private void handlePaymentError(ClientHttpResponse response){
        try {
            byte[] body = response.getBody().readAllBytes();
            PaymentConfirmErrorCode errorCode = objectMapper.readValue(body, PaymentConfirmErrorCode.class);
            throw new PaymentException(errorCode);
        } catch(IOException e) {
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
        }
    }

    private void validatePaymentStatus(String status) {
        if (!PAYMENT_SUCCESS_STATUS.equals(status)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_FAIL);
        }
    }
}
