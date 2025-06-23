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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;

    public PaymentCreateResponse createPayment(String username, PaymentInfoRequest paymentInfoRequest) {
        Member member = memberRepository.findByUsername(username).
                orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Payment payment = savePayment(member, paymentInfoRequest);

        return PaymentCreateResponse.builder()
                .paymentId(payment.getPaymentId())
                .build();
    }

    private Payment savePayment(Member member, PaymentInfoRequest paymentInfoRequest) {
        Order order = orderRepository.findById(paymentInfoRequest.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        Payment payment = Payment.builder()
                .member(member)
                .order(order)
                // TODO: 쿠폰 관련 로직 추후 구현
                .discountValue(0)
                .totalCost(paymentInfoRequest.getTotalCost())
                .paymentMethod(PaymentMethod.valueOf(paymentInfoRequest.getPaymentMethod()))
                .paymentStatus(PaymentStatus.valueOf(paymentInfoRequest.getPaymentStatus()))
                .storeRequest(paymentInfoRequest.getStoreRequest())
                .riderRequest(paymentInfoRequest.getRiderRequest())
                .build();
        return paymentRepository.save(payment);
    }

    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
