package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.rider.dto.ModifyWorkingRequest;
import com.idukbaduk.itseats.rider.dto.WorkingInfoResponse;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderRepository riderRepository;
    private final MemberRepository memberRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public WorkingInfoResponse modifyWorking(String username, ModifyWorkingRequest modifyWorkingRequest) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        Rider rider = riderRepository.findByMember(member)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));

        rider.modifyIsWorking(modifyWorkingRequest.getIsWorking());

        return WorkingInfoResponse.builder()
                .isWorking(rider.getIsWorking())
                .build();
    }

    @Transactional
    public void acceptDelivery(String username, Long orderId) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Rider rider = riderRepository.findByMember(member)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateOrderStatusAccept(rider, OrderStatus.RIDER_READY);
    }

    @Transactional
    public void updateOrderStatusAfterAccept(String username, Long orderId, OrderStatus orderStatus) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Rider rider = riderRepository.findByMember(member)
                .orElseThrow(() -> new RiderException(RiderErrorCode.RIDER_NOT_FOUND));

        Order order = orderRepository.findByRiderAndOrderId(rider, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(orderStatus);
    }
}
