package com.idukbaduk.itseats.payment.service;

import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.entity.PaymentCoupon;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final PaymentCouponRepository paymentCouponRepository;
    private final CouponPolicyService couponPolicyService;

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

        int discountValue = 0;
        MemberCoupon memberCoupon = null;

        if (paymentInfoRequest.getMemberCouponId() != null) {
            memberCoupon = memberCouponRepository.findById(paymentInfoRequest.getMemberCouponId())
                    .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

            discountValue = couponPolicyService.applyCouponDiscount(memberCoupon, member, order.getOrderPrice());
        }

        Payment payment = Payment.builder()
                .member(member)
                .order(order)
                .discountValue(discountValue)
                .totalCost(paymentInfoRequest.getTotalCost() - discountValue)
                .paymentMethod(PaymentMethod.valueOf(paymentInfoRequest.getPaymentMethod()))
                .paymentStatus(PaymentStatus.valueOf(paymentInfoRequest.getPaymentStatus()))
                .storeRequest(paymentInfoRequest.getStoreRequest())
                .riderRequest(paymentInfoRequest.getRiderRequest())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        if (memberCoupon != null) {
            PaymentCoupon paymentCoupon = PaymentCoupon.builder()
                    .payment(savedPayment)
                    .usedCoupon(memberCoupon)
                    .build();

            paymentCouponRepository.save(paymentCoupon);
            memberCoupon.markAsUsed();
        }
        return savedPayment;
    }

    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }
}
