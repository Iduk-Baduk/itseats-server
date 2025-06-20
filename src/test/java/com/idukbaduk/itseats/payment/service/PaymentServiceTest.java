package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("결제 정보를 성공적으로 반환")
    void getPaymentByOrder_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .build();
        Payment payment = Payment.builder()
                .paymentId(1L)
                .order(order)
                .build();

        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        // when
        Payment result = paymentService.getPaymentByOrder(order);

        // then
        assertThat(result).isEqualTo(payment);
    }

    @Test
    @DisplayName("존재하지 않는 결제 조회시 예외 발생")
    void getPaymentByOrder_notExist() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .build();

        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.getPaymentByOrder(order))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining(PaymentErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }
}