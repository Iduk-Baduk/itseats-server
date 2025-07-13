package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.coupon.repository.PaymentCouponRepository;
import com.idukbaduk.itseats.coupon.service.CouponPolicyService;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.dto.PaymentCreateResponse;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.entity.enums.PaymentMethod;
import com.idukbaduk.itseats.payment.entity.enums.PaymentStatus;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.payment.service.client.PaymentClient;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Transactional
class PaymentOrderIntegrationTest {

    @Mock
    private PaymentClient paymentClient;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @Mock
    private PaymentCouponRepository paymentCouponRepository;

    @Mock
    private CouponPolicyService couponPolicyService;

    @InjectMocks
    private PaymentService paymentService;

    private final String username = "testuser";
    private Member member;
    private Store store;
    private Order order;
    private Order orderPending;
    private PaymentInfoRequest paymentInfoRequest;
    private PaymentConfirmRequest paymentConfirmRequest;
    private MemberCoupon memberCoupon;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 설정
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .name("테스트 사용자")
                .phone("010-1234-5678")
                .build();

        store = Store.builder()
                .storeId(1L)
                .storeName("테스트 매장")
                .storePhone("02-1234-5678")
                .build();

        order = Order.builder()
                .orderId(1L)
                .orderNumber("A1234B")
                .orderPrice(8000)
                .orderStatus(OrderStatus.WAITING)
                .member(member)
                .store(store)
                .build();

        orderPending = Order.builder()
                .orderId(1L)
                .orderNumber("A1234B")
                .orderPrice(8000)
                .orderStatus(OrderStatus.PENDING)
                .member(member)
                .store(store)
                .build();

        paymentInfoRequest = PaymentInfoRequest.builder()
                .orderId(1L)
                .memberCouponId(null)
                .totalCost(10000L)
                .paymentMethod(PaymentMethod.COUPAY.name())
                .storeRequest("맛있게 만들어주세요")
                .riderRequest("문 앞에 두고 가주세요")
                .build();

        paymentConfirmRequest = PaymentConfirmRequest.builder()
                .tossPaymentKey("test_payment_key_123")
                .tossOrderId("test_order_123")
                .amount(10000L)
                .build();

        coupon = Coupon.builder()
                .couponId(1L)
                .discountValue(1000)
                .build();

        memberCoupon = MemberCoupon.builder()
                .memberCouponId(1L)
                .member(member)
                .coupon(coupon)
                .isUsed(false)
                .build();
    }

    @Test
    @DisplayName("결제 생성 후 주문 상태가 WAITING으로 유지되는지 확인")
    void createPayment_OrderStatusRemainsWaiting() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return Payment.builder()
                    .paymentId(1L)
                    .member(payment.getMember())
                    .order(payment.getOrder())
                    .discountValue(payment.getDiscountValue())
                    .totalCost(payment.getTotalCost())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .storeRequest(payment.getStoreRequest())
                    .riderRequest(payment.getRiderRequest())
                    .build();
        });

        // when
        PaymentCreateResponse response = paymentService.createPayment(username, paymentInfoRequest);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getPaymentId()).isEqualTo(1L);
        
        // 주문 상태가 WAITING으로 유지되는지 확인
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.WAITING);
        
        // 결제 상태가 PENDING인지 확인
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        assertThat(savedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    @DisplayName("결제 확인 후 결제 상태가 COMPLETED로 변경되는지 확인")
    void confirmPayment_PaymentStatusChangesToCompleted() {
        // given
        Payment payment = Payment.builder()
                .paymentId(1L)
                .member(member)
                .order(orderPending)
                .totalCost(10000L)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.findByPaymentIdAndUsername(username, 1L))
                .thenReturn(Optional.of(payment));
        when(paymentClient.confirmPayment(paymentConfirmRequest))
                .thenReturn(com.idukbaduk.itseats.payment.dto.PaymentClientResponse.builder()
                        .tossPaymentKey("test_payment_key_123")
                        .tossOrderId("test_order_123")
                        .totalAmount(10000L)
                        .build());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        // when
        paymentService.confirmPayment(username, 1L, paymentConfirmRequest);

        // then
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment confirmedPayment = paymentCaptor.getValue();
        
        assertThat(confirmedPayment.getPaymentStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(confirmedPayment.getTossPaymentKey()).isEqualTo("test_payment_key_123");
        assertThat(confirmedPayment.getTossOrderId()).isEqualTo("test_order_123");
    }

    @Test
    @DisplayName("쿠폰 사용 시 할인 금액이 올바르게 적용되는지 확인")
    void createPaymentWithCoupon_DiscountAppliedCorrectly() {
        // given
        paymentInfoRequest = PaymentInfoRequest.builder()
                .orderId(1L)
                .memberCouponId(1L)
                .totalCost(10000L)
                .paymentMethod(PaymentMethod.COUPAY.name())
                .storeRequest("맛있게 만들어주세요")
                .riderRequest("문 앞에 두고 가주세요")
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(memberCouponRepository.findById(1L)).thenReturn(Optional.of(memberCoupon));
        when(couponPolicyService.applyCouponDiscount(any(), any(), anyInt())).thenReturn(1000);
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return Payment.builder()
                    .paymentId(1L)
                    .member(payment.getMember())
                    .order(payment.getOrder())
                    .discountValue(payment.getDiscountValue())
                    .totalCost(payment.getTotalCost())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .storeRequest(payment.getStoreRequest())
                    .riderRequest(payment.getRiderRequest())
                    .build();
        });
        when(paymentCouponRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // when
        PaymentCreateResponse response = paymentService.createPayment(username, paymentInfoRequest);

        // then
        assertThat(response).isNotNull();
        
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        
        assertThat(savedPayment.getDiscountValue()).isEqualTo(1000);
        assertThat(savedPayment.getTotalCost()).isEqualTo(9000L); // 10000 - 1000
    }

    @Test
    @DisplayName("결제 금액 불일치 시 예외 발생 확인")
    void confirmPayment_AmountMismatchThrowsException() {
        // given
        Payment payment = Payment.builder()
                .paymentId(1L)
                .member(member)
                .order(order)
                .totalCost(10000L)
                .paymentStatus(PaymentStatus.PENDING)
                .build();

        PaymentConfirmRequest invalidRequest = PaymentConfirmRequest.builder()
                .tossPaymentKey("test_payment_key_123")
                .tossOrderId("test_order_123")
                .amount(9000L) // 실제 결제 금액과 다름
                .build();

        when(paymentRepository.findByPaymentIdAndUsername(username, 1L))
                .thenReturn(Optional.of(payment));
        when(paymentClient.confirmPayment(invalidRequest))
                .thenReturn(com.idukbaduk.itseats.payment.dto.PaymentClientResponse.builder()
                        .tossPaymentKey("test_payment_key_123")
                        .tossOrderId("test_order_123")
                        .totalAmount(9000L)
                        .build());

        // when & then
        assertThatThrownBy(() -> paymentService.confirmPayment(username, 1L, invalidRequest))
                .isInstanceOf(com.idukbaduk.itseats.payment.error.PaymentException.class);
    }

    @Test
    @DisplayName("결제 생성 후 주문과 결제가 올바르게 연동되는지 확인")
    void createPayment_OrderAndPaymentLinkedCorrectly() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            return Payment.builder()
                    .paymentId(1L)
                    .member(payment.getMember())
                    .order(payment.getOrder())
                    .discountValue(payment.getDiscountValue())
                    .totalCost(payment.getTotalCost())
                    .paymentMethod(payment.getPaymentMethod())
                    .paymentStatus(payment.getPaymentStatus())
                    .storeRequest(payment.getStoreRequest())
                    .riderRequest(payment.getRiderRequest())
                    .build();
        });

        // when
        PaymentCreateResponse response = paymentService.createPayment(username, paymentInfoRequest);

        // then
        assertThat(response).isNotNull();
        
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();
        
        // 주문과 결제가 올바르게 연동되었는지 확인
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getMember()).isEqualTo(member);
        assertThat(savedPayment.getTotalCost()).isEqualTo(paymentInfoRequest.getTotalCost());
        assertThat(savedPayment.getPaymentMethod()).isEqualTo(PaymentMethod.valueOf(paymentInfoRequest.getPaymentMethod()));
        assertThat(savedPayment.getStoreRequest()).isEqualTo(paymentInfoRequest.getStoreRequest());
        assertThat(savedPayment.getRiderRequest()).isEqualTo(paymentInfoRequest.getRiderRequest());
    }
} 
