package com.idukbaduk.itseats.rider.service;

import com.idukbaduk.itseats.member.entity.Member;
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
class RiderServiceTest {

    @Mock
    private RiderRepository riderRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private RiderService riderService;

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
    @DisplayName("출근 상태 전환 성공")
    void modifyWorking_true_sueccess() {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(true)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));

        // when
        WorkingInfoResponse response = riderService.modifyWorking(username, request);

        // then
        assertThat(response.getIsWorking()).isEqualTo(request.getIsWorking());
    }

    @Test
    @DisplayName("퇴근 상태 전환 성공")
    void modifyWorking_false_sueccess() {
        // given
        ModifyWorkingRequest request = ModifyWorkingRequest.builder()
                .isWorking(false)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));

        // when
        WorkingInfoResponse response = riderService.modifyWorking(username, request);

        // then
        assertThat(response.getIsWorking()).isEqualTo(request.getIsWorking());
    }

    @Test
    @DisplayName("존재하지 않는 라이더 조회시 예외 발생")
    void modifyWorking_notExistRider() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderService.modifyWorking(username, ModifyWorkingRequest.builder().build()))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문 상태를 배차 완료 상태로 변경에 성공")
    void acceptDelivery_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.COOKED)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        riderService.acceptDelivery(username, 1L);

        // then
        assertThat(order.getOrderId()).isEqualTo(1L);
        assertThat(order.getRider()).isEqualTo(rider);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.RIDER_READY);
    }

    @Test
    @DisplayName("주문이 직전 단계가 아닌 경우 주문 상태를 변경하면 예외 발생")
    void acceptDelivery_acceptStatusFail() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.COOKING)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        assertThatThrownBy(() -> riderService.acceptDelivery(username, 1L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL.getMessage());
    }

    @Test
    @DisplayName("라이더가 이미 배차된 주문의 주문 상태를 변경하면 예외 발생")
    void acceptDelivery_alreadyAssignedRider() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.COOKED)
                .rider(rider)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        assertThatThrownBy(() -> riderService.acceptDelivery(username, 1L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_ALREADY_ASSIGNED.getMessage());
    }

    @Test
    @DisplayName("주문 상태를 배달 완료 상태로 변경에 성공")
    void updateOrderStatusAfterAccept_delivered_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.DELIVERING)
                .rider(rider)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));

        // when
        riderService.updateOrderStatusAfterAccept(username, 1L, OrderStatus.DELIVERED);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("주문이 직전 단계가 아닌 경우 주문 상태를 변경하면 예외 발생")
    void updateOrderStatusAfterAccept_deliveredFail() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.COOKING)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));

        // when
        assertThatThrownBy(() -> riderService.updateOrderStatusAfterAccept(username, 1L, OrderStatus.DELIVERED))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL.getMessage());
    }

    @Test
    @DisplayName("배차되지 않은 주문의 상태를 변경하는 경우 예외 발생")
    void updateOrderStatusAfterAccept_getOrderFail() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(riderRepository.findByMember(member)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderService.updateOrderStatusAfterAccept(username, 1L, OrderStatus.DELIVERED))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }
}
