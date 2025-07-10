package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.order.service.RiderOrderService;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.entity.enums.AssignmentStatus;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderAssignmentRepository;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import org.junit.jupiter.api.BeforeEach;
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
class RiderOrderServiceTest {

    @Mock
    private RiderRepository riderRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private RiderService riderService;

    @InjectMocks
    private RiderOrderService riderOrderService;

    private final String username = "testuser";
    private Member member;
    private Rider rider;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();

        rider = Rider.builder()
                .member(member)
                .riderId(1L)
                .build();
    }

    @Test
    @DisplayName("배달 수락 성공")
    void acceptOrder_success() {
        // given
        Long orderId = 1L;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.COOKED)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(order));

        // when
        riderService.updateRiderAssignment(rider, order, AssignmentStatus.ACCEPTED);
        riderOrderService.acceptDelivery(username, orderId);

        // then
        assertThat(order.getRider()).isEqualTo(rider);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.RIDER_READY);
    }

    @Test
    @DisplayName("배달 수락 실패 - 라이더를 찾을 수 없음")
    void acceptOrder_riderNotFound() {
        // given
        Long orderId = 1L;
        when(riderRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderOrderService.acceptDelivery(username, orderId))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배달 수락 실패 - 주문을 찾을 수 없음")
    void acceptOrder_orderNotFound() {
        // given
        Long orderId = 1L;
        Member mockMember = Member.builder().username(username).build();
        Rider mockRider = Rider.builder().member(mockMember).build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(mockRider));
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderOrderService.acceptDelivery(username, orderId))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배달 수락 실패 - 주문이 이미 배정됨")
    void acceptOrder_orderAlreadyAssigned() {
        // given
        Long orderId = 1L;

        Member mockMember = Member.builder().username(username).build();
        Rider tryingRider = Rider.builder().riderId(1L).member(mockMember).build();
        Rider assignedRider = Rider.builder().riderId(2L).build();

        Order alreadyAssignedOrder = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.COOKED)
                .rider(assignedRider)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(tryingRider));
        when(orderRepository.findByIdForUpdate(orderId)).thenReturn(Optional.of(alreadyAssignedOrder));

        // when & then
        assertThatThrownBy(() -> riderOrderService.acceptDelivery(username, orderId))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_ALREADY_ASSIGNED.getMessage());
    }
}
