package com.idukbaduk.itseats.payment.service.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.payment.dto.PaymentClientResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentConfirmErrorCode;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.Objects;

@Slf4j
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

            validatePaymentClientResponse(clientResponse);
            validatePaymentStatus(clientResponse.getStatus());
            return clientResponse;
        } catch (ResourceAccessException e) {
            log.error("[토스 결제 승인 ResourceAccessException] {}", e.getMessage(), e);
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
        } catch (Exception e) {
            log.error("[토스 결제 승인 전체 예외] {}", e.getMessage(), e);
            throw e;
        }
    }

    private void handlePaymentError(ClientHttpResponse response){
        try {
            System.out.println("[토스 결제 에러] onStatus 핸들러 진입");
            byte[] body = response.getBody().readAllBytes();
            String errorBody = new String(body);
            log.error("[토스 결제 에러] 응답 바디 원본: {}", errorBody);
            System.out.println("[토스 결제 에러] 응답 바디 원본: " + errorBody);
            try {
                PaymentConfirmErrorCode errorCode = objectMapper.readValue(body, PaymentConfirmErrorCode.class);
                log.error("[토스 결제 에러] 매핑된 코드: {}", errorCode);
                System.out.println("[토스 결제 에러] 매핑된 코드: " + errorCode);
                throw new PaymentException(errorCode);
            } catch(Exception mappingEx) {
                log.error("[토스 결제 에러] 응답 바디(매핑 실패): {}", errorBody, mappingEx);
                System.out.println("[토스 결제 에러] 응답 바디(매핑 실패): " + errorBody);
                throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
            }
        } catch(IOException e) {
            log.error("[토스 결제 에러] 응답 바디 읽기 실패", e);
            System.out.println("[토스 결제 에러] 응답 바디 읽기 실패: " + e.getMessage());
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
        }
    }

    private void validatePaymentClientResponse(PaymentClientResponse clientResponse) {
        if (clientResponse == null) {
            log.error("[토스 결제 validatePaymentClientResponse] clientResponse가 null입니다.");
            throw new PaymentException(PaymentErrorCode.TOSS_PAYMENT_SERVER_ERROR);
        }
    }

    private void validatePaymentStatus(String status) {
        if (!PAYMENT_SUCCESS_STATUS.equals(status)) {
            log.error("[토스 결제 validatePaymentStatus] status가 비정상: {}", status);
            throw new PaymentException(PaymentErrorCode.PAYMENT_FAIL);
        }
    }
}
