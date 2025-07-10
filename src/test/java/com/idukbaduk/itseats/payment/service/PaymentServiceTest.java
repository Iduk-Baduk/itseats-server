package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.coupon.entity.PaymentCoupon;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.coupon.repository.PaymentCouponRepository;
import com.idukbaduk.itseats.coupon.service.CouponPolicyService;
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
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.entity.PaymentCoupon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @Mock
    private PaymentCouponRepository paymentCouponRepository;

    @Mock
    private CouponPolicyService couponPolicyService;

    @InjectMocks
    private PaymentService paymentService;

    private final String username = "testuser";
    private Order order;
    private Member member;
    private PaymentInfoRequest paymentInfoRequest;
    private MemberCoupon memberCoupon;
    private Coupon coupon;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();
        order = Order.builder()
                .orderId(1L)
                .orderPrice(8000)
                .build();

        paymentInfoRequest = PaymentInfoRequest.builder()
                .orderId(1L)
                .memberCouponId(null) // 기본 테스트는 쿠폰 미사용
                .totalCost(10000L)
                .paymentMethod(PaymentMethod.COUPAY.name())
                .storeRequest("맛있게 만들어주세요")
                .riderRequest("문 앞에 두고 가주세요")
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
    @DisplayName("쿠폰을 사용하지 않는 결제 정보 저장 성공")
    void createPayment_withoutCoupon() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<Payment> captor = ArgumentCaptor.forClass(Payment.class);

        // when
        PaymentCreateResponse response = paymentService.createPayment(username, paymentInfoRequest);

        // then
        assertThat(response).isNotNull();
        verify(paymentRepository).save(captor.capture());
        Payment savedPayment = captor.getValue();

        assertThat(savedPayment.getMember()).isEqualTo(member);
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getDiscountValue()).isEqualTo(0);
        assertThat(savedPayment.getTotalCost()).isEqualTo(paymentInfoRequest.getTotalCost());
    }

    @Test
    @DisplayName("쿠폰을 사용하는 결제 정보 저장 성공")
    void createPayment_withCoupon() {
        // given
        Long memberCouponId = 1L;
        paymentInfoRequest = PaymentInfoRequest.builder()
                .orderId(1L)
                .memberCouponId(memberCouponId)
                .totalCost(10000L)
                .paymentMethod(PaymentMethod.COUPAY.name())
                .storeRequest("맛있게 만들어주세요")
                .riderRequest("문 앞에 두고 가주세요")
                .build();

        when(memberRepository.findByUsername(username))
                .thenReturn(Optional.of(member));
        when(orderRepository.findById(1L))
                .thenReturn(Optional.of(order));
        when(memberCouponRepository.findById(memberCouponId))
                .thenReturn(Optional.of(memberCoupon));
        when(couponPolicyService.applyCouponDiscount(any(MemberCoupon.class), any(Member.class), anyInt()))
                .thenReturn(1000);
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(i -> i.getArgument(0));
        when(paymentCouponRepository.save(any(PaymentCoupon.class)))
                .thenAnswer(i -> i.getArgument(0));

        // when
        PaymentCreateResponse response = paymentService.createPayment(username, paymentInfoRequest);

        // then
        assertThat(response).isNotNull();

        verify(memberCouponRepository).findById(memberCouponId);
        verify(paymentRepository).save(any(Payment.class));
        verify(paymentCouponRepository).save(any(PaymentCoupon.class));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment savedPayment = paymentCaptor.getValue();

        assertThat(savedPayment.getMember()).isEqualTo(member);
        assertThat(savedPayment.getOrder()).isEqualTo(order);
        assertThat(savedPayment.getDiscountValue()).isEqualTo(1000);
        assertThat(savedPayment.getTotalCost()).isEqualTo(paymentInfoRequest.getTotalCost() - 1000);
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
