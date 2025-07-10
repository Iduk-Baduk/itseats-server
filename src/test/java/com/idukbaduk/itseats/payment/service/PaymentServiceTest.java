package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.dto.PaymentCreateResponse;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.entity.enums.PaymentMethod;
import com.idukbaduk.itseats.payment.entity.enums.PaymentStatus;
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
    private MemberRepository memberRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private PaymentService paymentService;

    private final String username = "testuser";
    private Order order;
    private Member member;
    private PaymentInfoRequest paymentInfoRequest;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();
        order = Order.builder()
                .orderId(1L)
                .build();

        paymentInfoRequest = PaymentInfoRequest.builder()
                .orderId(1L)
                .totalCost(10000L)
                .paymentMethod(PaymentMethod.COUPAY.name())
                .storeRequest("맛있게 만들어주세요")
                .riderRequest("문 앞에 두고 가주세요")
                .build();
    }

    @Test
    @DisplayName("결제 정보 저장 성공")
    void createPayment() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(orderRepository.findById(1L)).thenReturn(Optional.ofNullable(order));
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
        assertThat(savedPayment.getTotalCost()).isEqualTo(paymentInfoRequest.getTotalCost());
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.valueOf(paymentInfoRequest.getPaymentMethod()));
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
        assertThat(savedPayment.getStoreRequest()).isEqualTo(paymentInfoRequest.getStoreRequest());
        assertThat(savedPayment.getRiderRequest()).isEqualTo(paymentInfoRequest.getRiderRequest());
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회시 예외 발생")
    void createPayment_notExistOrder() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.createPayment(username, paymentInfoRequest))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());

        verify(orderRepository).findById(1L);
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
    void getPaymentByOrder_notExistPayment() {
        // given
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> paymentService.getPaymentByOrder(order))
                .isInstanceOf(PaymentException.class)
                .hasMessageContaining(PaymentErrorCode.PAYMENT_NOT_FOUND.getMessage());
    }
}