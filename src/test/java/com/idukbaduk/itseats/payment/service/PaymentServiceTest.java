package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.service.OrderService;
import com.idukbaduk.itseats.payment.dto.PaymentCreateResponse;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.entity.enums.PaymentMethod;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberService memberService;

    @Mock
    private OrderService orderService;

    @InjectMocks
    private PaymentService paymentService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .orderId(1L)
                .build();
    }

    @Test
    @DisplayName("결제 정보 저장 성공")
    void createPayment() {
        // given
        String username = "testuser";
        Member member = Member.builder()
                .username(username)
                .build();

        Long orderId = 1L;
        int totalCost = 10000;
        String paymentMethod = PaymentMethod.COUPAY.name();
        String storeRequest = "맛있게 만들어주세요";
        String riderRequest = "문 앞에 두고 가주세요";
        PaymentInfoRequest paymentInfoRequest = PaymentInfoRequest.builder()
                .orderId(orderId)
                .totalCost(totalCost)
                .paymentMethod(paymentMethod)
                .storeRequest(storeRequest)
                .riderRequest(riderRequest)
                .build();

        when(memberService.getMemberByUsername(username)).thenReturn(member);
        when(orderService.getOrder(orderId)).thenReturn(order);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

        // when
        PaymentCreateResponse response = paymentService.createPayment(username, paymentInfoRequest);

        // then - response
        assertThat(response).isNotNull();

        // then - save
        verify(paymentRepository).save(captor.capture());
        Payment savedPayment = captor.getValue();

        assertThat(savedPayment.getMember()).isEqualTo(member);
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getTotalCost()).isEqualTo(totalCost);
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.valueOf(paymentMethod));
        assertThat(savedPayment.getStoreRequest()).isEqualTo(storeRequest);
        assertThat(savedPayment.getRiderRequest()).isEqualTo(riderRequest);
    }

    @Test
    @DisplayName("결제 정보를 성공적으로 반환")
    void getPaymentByOrder_success() {
        // given
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
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.getPaymentByOrder(order))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining(PaymentErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }
}