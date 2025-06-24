package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.OrderDetailResponse;
import com.idukbaduk.itseats.order.dto.OrderMenuItemDTO;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.member.entity.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class OwnerOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OwnerOrderService ownerOrderService;

    @Test
    @DisplayName("주문 상세 조회 성공")
    void getOrderDetail_success() {
        // given
        Member member = Member.builder().name("구름톤").build();

        OrderMenu menu1 = OrderMenu.builder()
                .menuName("아메리카노")
                .quantity(2)
                .price(4000)
                .menuOption("샷추가,사이즈업")
                .menu(null) // 실제 Menu 엔티티가 필요하면 추가
                .build();

        OrderMenu menu2 = OrderMenu.builder()
                .menuName("에스프레소")
                .quantity(1)
                .price(1500)
                .menuOption("")
                .menu(null)
                .build();

        Order order = Order.builder()
                .orderId(3L)
                .orderNumber("GRMT0N")
                .member(member)
                .orderStatus(OrderStatus.WAITING)
                .orderReceivedTime(LocalDateTime.of(2025, 5, 5, 0, 0))
                .orderMenus(List.of(menu1, menu2))
                .build();

        when(orderRepository.findDetailById(3L)).thenReturn(Optional.of(order));

        // when
        OrderDetailResponse response = ownerOrderService.getOrderDetail(3L);

        // then
        assertThat(response.getOrderId()).isEqualTo(3L);
        assertThat(response.getOrderNumber()).isEqualTo("GRMT0N");
        assertThat(response.getMemberName()).isEqualTo("구름톤");
        assertThat(response.getOrderStatus()).isEqualTo("WAITING");
        assertThat(response.getMenuItems()).hasSize(2);
        assertThat(response.getMenuItems().get(0).getMenuName()).isEqualTo("아메리카노");
    }

    @Test
    @DisplayName("주문이 없으면 예외 발생")
    void getOrderDetail_notFound() {
        when(orderRepository.findDetailById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> ownerOrderService.getOrderDetail(999L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }
}
