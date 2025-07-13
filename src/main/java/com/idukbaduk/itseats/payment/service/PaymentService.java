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
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.dto.PaymentClientResponse;
import com.idukbaduk.itseats.payment.dto.PaymentConfirmRequest;
import com.idukbaduk.itseats.payment.dto.PaymentCreateResponse;
import com.idukbaduk.itseats.payment.dto.PaymentInfoRequest;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.entity.enums.PaymentMethod;
import com.idukbaduk.itseats.payment.entity.enums.PaymentStatus;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.payment.service.client.PaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.idukbaduk.itseats.payment.dto.TossPaymentConfirmRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final PaymentCouponRepository paymentCouponRepository;
    private final CouponPolicyService couponPolicyService;

    public PaymentCreateResponse createPayment(String username, PaymentInfoRequest paymentInfoRequest) {
        log.info("결제 생성 서비스 시작 - username: {}, orderId: {}", username, paymentInfoRequest.getOrderId());
        
        try {
            Member member = memberRepository.findByUsername(username).
                    orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
            log.info("회원 정보 조회 성공 - memberId: {}", member.getMemberId());

            Payment payment = savePayment(member, paymentInfoRequest);
            log.info("결제 정보 저장 성공 - paymentId: {}", payment.getPaymentId());

            PaymentCreateResponse response = PaymentCreateResponse.builder()
                    .paymentId(payment.getPaymentId())
                    .build();
            
            log.info("결제 생성 완료 - paymentId: {}", response.getPaymentId());
            return response;
        } catch (Exception e) {
            log.error("결제 생성 중 오류 발생 - username: {}, orderId: {}, error: {}", 
                    username, paymentInfoRequest.getOrderId(), e.getMessage(), e);
            throw e;
        }
    }

    private Payment savePayment(Member member, PaymentInfoRequest paymentInfoRequest) {
        log.info("결제 정보 저장 시작 - orderId: {}", paymentInfoRequest.getOrderId());
        
        Order order = orderRepository.findById(paymentInfoRequest.getOrderId())
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        log.info("주문 정보 조회 성공 - orderId: {}, orderNumber: {}", order.getOrderId(), order.getOrderNumber());

        long discountValue = 0;
        MemberCoupon memberCoupon = null;

        if (paymentInfoRequest.getMemberCouponId() != null) {
            log.info("쿠폰 적용 시작 - memberCouponId: {}", paymentInfoRequest.getMemberCouponId());
            memberCoupon = memberCouponRepository.findById(paymentInfoRequest.getMemberCouponId())
                    .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

            discountValue = couponPolicyService.applyCouponDiscount(memberCoupon, member, order.getOrderPrice());
            log.info("쿠폰 할인 적용 완료 - discountValue: {}", discountValue);
        }

        Payment payment = Payment.builder()
                .member(member)
                .order(order)
                .discountValue(discountValue)
                .totalCost(paymentInfoRequest.getTotalCost()) // discountValue를 빼지 않고 그대로 저장
                .paymentMethod(PaymentMethod.valueOf(paymentInfoRequest.getPaymentMethod()))
                .paymentStatus(PaymentStatus.PENDING)
                .storeRequest(paymentInfoRequest.getStoreRequest())
                .riderRequest(paymentInfoRequest.getRiderRequest())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 정보 저장 완료 - paymentId: {}, totalCost: {}", savedPayment.getPaymentId(), savedPayment.getTotalCost());

        if (memberCoupon != null) {
            PaymentCoupon paymentCoupon = PaymentCoupon.builder()
                    .payment(savedPayment)
                    .usedCoupon(memberCoupon)
                    .build();

            paymentCouponRepository.save(paymentCoupon);
            memberCoupon.markAsUsed();
            log.info("쿠폰 사용 처리 완료 - memberCouponId: {}", memberCoupon.getMemberCouponId());
        }
        return savedPayment;
    }

    public Payment getPaymentByOrder(Order order) {
        return paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    public void confirmPayment(String username, Long paymentId, PaymentConfirmRequest paymentConfirmRequest) {
        log.info("결제 확인 서비스 시작 - username: {}, paymentId: {}", username, paymentId);
        
        try {
            Payment payment = paymentRepository.findByPaymentIdAndUsername(username, paymentId)
                    .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
            log.info("결제 정보 조회 성공 - payment: {}", payment.getPaymentId());

            log.info("[결제 승인] paymentClient.confirmPayment 호출 전: paymentId={}, username={}", paymentId, username);
            try {
                PaymentClientResponse clientResponse = paymentClient.confirmPayment(paymentConfirmRequest);
                log.info("[결제 승인] paymentClient.confirmPayment 호출 후: clientResponse={}", clientResponse);
                validateAmount(payment.getTotalCost(), clientResponse.getTotalAmount());

                payment.confirm(clientResponse.getTossPaymentKey(), clientResponse.getTossOrderId());
                Order order = payment.getOrder();
                order.updateStatus(OrderStatus.WAITING);
                orderRepository.save(order);
                paymentRepository.save(payment);
            } catch (Exception e) {
                log.error("[결제 승인] paymentClient.confirmPayment 예외 발생: {}", e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            log.error("결제 확인 중 오류 발생 - username: {}, paymentId: {}, error: {}", 
                    username, paymentId, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 토스페이먼츠 결제 확인 (단순화된 1단계 플로우)
     */
    @Transactional
    public void confirmTossPayment(String username, TossPaymentConfirmRequest request) {
        log.info("토스페이먼츠 결제 확인 시작 - username: {}, orderId: {}, paymentKey: {}", 
                username, request.getOrderId(), request.getPaymentKey());
        
        try {
            // 1. 회원 정보 조회
            Member member = memberRepository.findByUsername(username)
                    .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
            log.info("회원 정보 조회 성공 - memberId: {}", member.getMemberId());

            // 2. 주문 정보 조회
            Order order = orderRepository.findById(request.getOrderIdForDb())
                    .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
            log.info("주문 정보 조회 성공 - orderId: {}, orderNumber: {}", order.getOrderId(), order.getOrderNumber());

            // 3. 토스페이먼츠 결제 확인
            PaymentConfirmRequest tossRequest = PaymentConfirmRequest.builder()
                    .tossPaymentKey(request.getPaymentKey())
                    .tossOrderId(request.getOrderId())
                    .amount(request.getAmount())
                    .build();
            
            log.info("토스페이먼츠 결제 확인 요청");
            PaymentClientResponse clientResponse = paymentClient.confirmPayment(tossRequest);
            log.info("토스페이먼츠 결제 확인 응답 - response: {}", clientResponse);

            // 4. 결제 정보 생성 및 저장
            Payment payment = createPaymentFromToss(member, order, request, clientResponse);
            log.info("결제 정보 생성 완료 - paymentId: {}", payment.getPaymentId());

            log.info("토스페이먼츠 결제 확인 완료 - paymentId: {}", payment.getPaymentId());
        } catch (Exception e) {
            log.error("토스페이먼츠 결제 확인 중 오류 발생 - username: {}, error: {}", 
                    username, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * 토스페이먼츠 응답으로부터 결제 정보 생성
     */
    private Payment createPaymentFromToss(Member member, Order order, 
                                        TossPaymentConfirmRequest request, 
                                        PaymentClientResponse clientResponse) {
        log.info("토스페이먼츠 응답으로 결제 정보 생성 시작");

        // 쿠폰 할인 계산
        long discountValue = 0;
        MemberCoupon memberCoupon = null;

        if (request.getMemberCouponId() != null) {
            log.info("쿠폰 적용 시작 - memberCouponId: {}", request.getMemberCouponId());
            memberCoupon = memberCouponRepository.findById(request.getMemberCouponId())
                    .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

            discountValue = couponPolicyService.applyCouponDiscount(memberCoupon, member, order.getOrderPrice());
            log.info("쿠폰 할인 적용 완료 - discountValue: {}", discountValue);
        }

        // 결제 금액 검증 (주문금액 – 할인 + 배송비)
        Long expectedAmount = order.getOrderPrice() - discountValue + order.getDeliveryFee();
        if (!expectedAmount.equals(request.getAmount())) {
            log.error("결제 금액 불일치 - expected: {}, actual: {}", expectedAmount, request.getAmount());
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 결제 정보 생성
        Payment payment = Payment.builder()
                .member(member)
                .order(order)
                .discountValue(discountValue)
                .totalCost(request.getAmount())
                .paymentMethod(PaymentMethod.COUPAY) // 토스페이먼츠는 COUPAY로 통일
                .paymentStatus(PaymentStatus.COMPLETED) // 토스페이먼츠 확인 완료 시 바로 COMPLETED
                .storeRequest(request.getStoreRequest())
                .riderRequest(request.getRiderRequest())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("결제 정보 저장 완료 - paymentId: {}", savedPayment.getPaymentId());

        // 쿠폰 사용 처리
        if (memberCoupon != null) {
            PaymentCoupon paymentCoupon = PaymentCoupon.builder()
                    .payment(savedPayment)
                    .usedCoupon(memberCoupon)
                    .build();

            paymentCouponRepository.save(paymentCoupon);
            memberCoupon.markAsUsed();
            log.info("쿠폰 사용 처리 완료 - memberCouponId: {}", memberCoupon.getMemberCouponId());
        }

        return savedPayment;
    }

    private void validateAmount(Long totalCost, Long tossAmount) {
        if (!totalCost.equals(tossAmount)) {
            throw new PaymentException(PaymentErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}
