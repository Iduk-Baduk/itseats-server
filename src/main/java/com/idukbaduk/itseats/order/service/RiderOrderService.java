package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.dto.OrderDetailsResponse;
import com.idukbaduk.itseats.order.dto.OrderItemDTO;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RiderOrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final RiderRepository riderRepository;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderDetails(String username, Long orderId) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Rider rider = riderRepository.findByMember(member)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));

        Order order = orderRepository.findByRiderAndOrderId(rider, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        return buildOrderDetails(order);
    }

    private OrderDetailsResponse buildOrderDetails(Order order) {
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        return OrderDetailsResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderStatus(order.getOrderStatus().name())
                .orderTime(order.getCreatedAt().toString())
                .totalPrice(order.getOrderPrice())
                .orderItems(buildOrderItems(order))
                .storePhone(order.getStore().getStorePhone())
                .memberPhone(order.getMember().getPhone())
                .storeRequest(payment.getStoreRequest())
                .riderRequest(payment.getRiderRequest())
                .build();
    }

    private List<OrderItemDTO> buildOrderItems(Order order) {
        return order.getOrderMenus().stream()
                .map(orderMenu -> OrderItemDTO.builder()
                        .menuName(orderMenu.getMenuName())
                        .quantity(orderMenu.getQuantity())
                        .menuPrice(orderMenu.getPrice())
                        .options(orderMenu.getMenuOption())
                        .build())
                .toList();
    }
}
