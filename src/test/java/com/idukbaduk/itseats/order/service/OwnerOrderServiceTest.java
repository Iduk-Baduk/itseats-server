package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class OwnerOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private OwnerOrderService ownerOrderService;

    private final String username = "testuser";

    @Test
    @DisplayName("가게 주문 목록 정상 조회")
    void getOrders_success() {
        // given
        OrderMenu orderMenu = OrderMenu.builder()
                .menuName("아메리카노")
                .quantity(2)
                .price(4000)
                .menuOption("샷추가")
                .build();

        Order order = Order.builder()
                .orderNumber("ORD123")
                .orderReceivedTime(LocalDateTime.of(2025, 6, 24, 12, 0))
                .orderStatus(OrderStatus.COMPLETED)
                .orderMenus(List.of(orderMenu))
                .build();

        Payment payment = Payment.builder()
                .storeRequest("빨리 주세요")
                .build();

        when(orderRepository.findAllWithMenusByStoreUsernameAndStoreId(username, 1L)).thenReturn(List.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        // when
        List<OrderReceptionResponse> result = ownerOrderService.getOrders(username, 1L);

        // then
        assertThat(result).hasSize(1);
        OrderReceptionResponse response = result.get(0);
        assertThat(response.getOrderNumber()).isEqualTo("ORD123");
        assertThat(response.getMenuCount()).isEqualTo(2);
        assertThat(response.getCustomerRequest()).contains("빨리 주세요");
        assertThat(response.getMenuItems()).hasSize(1);
    }

    @Test
    @DisplayName("주문이 없는 경우 빈 리스트 반환")
    void getOrders_empty() {
        when(orderRepository.findAllWithMenusByStoreUsernameAndStoreId(username, 1L)).thenReturn(Collections.emptyList());
        List<OrderReceptionResponse> result = ownerOrderService.getOrders(username, 1L);
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("결제 정보가 없는 경우 customerRequest는 빈 문자열")
    void getOrders_noPayment() {
        Order order = Order.builder()
                .orderNumber("ORD124")
                .orderReceivedTime(LocalDateTime.now())
                .orderStatus(OrderStatus.COMPLETED)
                .orderMenus(Collections.emptyList())
                .build();

        when(orderRepository.findAllWithMenusByStoreUsernameAndStoreId(username, 1L)).thenReturn(List.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.empty());

        List<OrderReceptionResponse> result = ownerOrderService.getOrders(username, 1L);
        assertThat(result.get(0).getCustomerRequest()).isEmpty();
    }

    @Test
    @DisplayName("주문 거절 성공")
    void rejectOrder_success() {
        // given
        Long orderId = 1L;
        String reason = "재고 부족";
        Order order = mock(Order.class);
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.of(order));

        // when
        OrderRejectResponse response = ownerOrderService.rejectOrder(username, orderId, reason);

        // then
        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getReason()).isEqualTo(reason);
        then(order).should().reject(reason);
    }

    @Test
    @DisplayName("주문 거절 시 주문이 존재하지 않으면 예외 발생")
    void rejectOrder_orderNotFound() {
        // given: 주문이 존재하지 않음
        Long orderId = 1L;
        String reason = "재고 부족";
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerOrderService.rejectOrder(username, orderId, reason))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문 수락 성공")
    void acceptOrder_success() {
        // given
        Long orderId = 1L;
        Order order = mock(Order.class);
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.of(order));

        // when
        OrderAcceptResponse response = ownerOrderService.acceptOrder(username, orderId);

        // then
        assertThat(response.isSuccess()).isTrue();
        then(order).should().updateStatus(OrderStatus.ACCEPTED);
    }

    @Test
    @DisplayName("주문 수락 시 주문이 존재하지 않으면 예외 발생")
    void acceptOrder_orderNotFound() {
        // given
        Long orderId = 1L;
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerOrderService.acceptOrder(username, orderId))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("조리 완료 성공")
    void markAsCooked_success() {
        // given
        Long orderId = 1L;
        Order order = mock(Order.class);
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.of(order));

        // when
        OrderCookedResponse response = ownerOrderService.markAsCooked(username, orderId);

        // then
        assertThat(response.isSuccess()).isTrue();
        then(order).should().updateStatus(OrderStatus.COOKED);
    }

    @Test
    @DisplayName("조리 완료 상태 변경 시 주문이 존재하지 않으면 예외 발생")
    void markAsCooked_orderNotFound() {
        // given
        Long orderId = 1L;
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerOrderService.markAsCooked(username, orderId))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예상 조리시간 설정 시 주문이 없으면 예외 발생")
    void setCookTime_orderNotFound() {
        // given
        Long orderId = 99L;
        int cookTime = 10;
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> ownerOrderService.setCookTime(username, orderId, cookTime))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("예상 조리 시간 설정 성공 - ACCEPTED 상태에서 COOKING 전이 가능")
    void setCookTime_success() {
        // given
        Long orderId = 1L;
        int cookTime = 15;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.ACCEPTED) // COOKING 전이 가능 상태
                .build();
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.of(order));

        // when
        CookTimeResponse response = ownerOrderService.setCookTime(username, orderId, cookTime);

        // then
        assertThat(response.getOrderId()).isEqualTo(orderId);
        assertThat(response.getDeliveryEta()).isNotNull();
    }

    @Test
    @DisplayName("예상 조리 시간 설정 실패 - COMPLETED 상태에서 COOKING 전이 불가")
    void setCookTime_fail_status_completed() {
        // given
        Long orderId = 1L;
        int cookTime = 15;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.COMPLETED) // COOKING 전이 불가 상태
                .build();
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> ownerOrderService.setCookTime(username, orderId, cookTime))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL.getMessage());
    }

    @Test
    @DisplayName("예상 조리 시간 설정 실패 - REJECTED 상태에서 COOKING 전이 불가")
    void setCookTime_fail_status_rejected() {
        // given
        Long orderId = 1L;
        int cookTime = 15;
        Order order = Order.builder()
                .orderId(orderId)
                .orderStatus(OrderStatus.REJECTED) // COOKING 전이 불가 상태
                .build();
        given(orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)).willReturn(Optional.of(order));

        // when & then
        assertThatThrownBy(() -> ownerOrderService.setCookTime(username, orderId, cookTime))
                .isInstanceOf(OrderException.class)
                .hasMessage(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL.getMessage());
    }
}
