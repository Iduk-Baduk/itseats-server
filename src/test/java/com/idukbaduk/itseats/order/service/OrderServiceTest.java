package com.idukbaduk.itseats.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.service.MemberAddressService;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.service.MenuService;
import com.idukbaduk.itseats.order.dto.MenuOptionDTO;
import com.idukbaduk.itseats.order.dto.OptionDTO;
import com.idukbaduk.itseats.order.dto.OrderMenuDTO;
import com.idukbaduk.itseats.order.dto.OrderNewRequest;
import com.idukbaduk.itseats.order.dto.OrderNewResponse;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderMenuRepository;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.service.StoreService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMenuRepository orderMenuRepository;

    @Mock
    private MenuService menuService;

    @Mock
    private StoreService storeService;

    @Mock
    private MemberService memberService;

    @Mock
    private MemberAddressService memberAddressService;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setup() {
        orderService = new OrderService(
                orderRepository,
                orderMenuRepository,
                menuService,
                storeService,
                memberService,
                memberAddressService,
                new ObjectMapper()
        );
    }

    @Test
    @DisplayName("주문 정보 상세 조회 성공")
    void getOrderNew_success() {
        // given
        String username = "testuser";
        Member mockMember = Member.builder()
                .username(username)
                .build();

        Store mockStore = Store.builder()
                .storeId(1L)
                .defaultDeliveryFee(3000)
                .onlyOneDeliveryFee(3500)
                .build();

        MemberAddress mockAddress = MemberAddress.builder()
                .addressId(1L)
                .mainAddress("서울시 구름구 구름로100번길 10")
                .detailAddress("100호")
                .build();

        OrderMenuDTO orderMenuDTO1 = OrderMenuDTO.builder()
                .menuId(1L)
                .menuName("아메리카노")
                .quantity(2)
                .menuOption(
                        List.of(MenuOptionDTO.builder()
                                .options(List.of(OptionDTO.builder()
                                        .optionPrice(1000)
                                        .build()))
                        .build()))
                .menuTotalPrice(2000)
                .build();
        OrderMenuDTO orderMenuDTO2 = OrderMenuDTO.builder()
                .menuId(2L)
                .menuName("라떼")
                .quantity(1)
                .menuOption(
                        List.of(MenuOptionDTO.builder()
                                .options(List.of(OptionDTO.builder()
                                        .optionPrice(500)
                                        .build()))
                                .build()))
                .menuTotalPrice(3000)
                .build();

        OrderNewRequest request = OrderNewRequest.builder()
                .addrId(1L)
                .storeId(1L)
                .deliveryType(DeliveryType.DEFAULT.name())
                .orderMenus(List.of(orderMenuDTO1, orderMenuDTO2))
                .build();

        when(memberService.getMemberByUsername(username)).thenReturn(mockMember);
        when(memberAddressService.getMemberAddress(1L)).thenReturn(mockAddress);
        when(storeService.getStore(1L)).thenReturn(mockStore);
        when(menuService.getMenu(1L)).thenReturn(new Menu());

        when(orderRepository.findAvgDeliveryTimeByType(DeliveryType.DEFAULT.name())).thenReturn(30L);
        when(orderRepository.findMinDeliveryTimeByType(DeliveryType.DEFAULT.name())).thenReturn(20);
        when(orderRepository.findMaxDeliveryTimeByType(DeliveryType.DEFAULT.name())).thenReturn(40);
        when(orderRepository.findMinDeliveryTimeByType(DeliveryType.ONLY_ONE.name())).thenReturn(25);
        when(orderRepository.findMaxDeliveryTimeByType(DeliveryType.ONLY_ONE.name())).thenReturn(45);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // when
        OrderNewResponse response = orderService.getOrderNew(username, request);

        // then
        assertThat(response.getOrderPrice()).isEqualTo(7000);
        assertThat(response.getDeliveryFee()).isEqualTo(3000);
        assertThat(response.getTotalCost()).isEqualTo(10000);
        assertThat(response.getDefaultTImeMin()).isEqualTo(20);
        assertThat(response.getOnlyOneTimeMax()).isEqualTo(45);

        verify(orderMenuRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("ObjectMapper가 null일 때 menuOption 직렬화 실패 예외 발생")
    void getOrderNew_serializationException() {
        // given
        OrderService exceptionOrderService = new OrderService(
                orderRepository,
                orderMenuRepository,
                menuService,
                storeService,
                memberService,
                memberAddressService,
                null
        );

        String username = "testuser";

        OrderMenuDTO menuDTO = OrderMenuDTO.builder()
                .menuId(1L)
                .menuName("메뉴")
                .quantity(1)
                .menuTotalPrice(1000)
                .menuOption(List.of(MenuOptionDTO.builder().build()))
                .build();
        OrderNewRequest request = OrderNewRequest.builder()
                .addrId(1L)
                .storeId(1L)
                .deliveryType(DeliveryType.DEFAULT.name())
                .orderMenus(List.of(menuDTO))
                .build();

        when(memberService.getMemberByUsername(any())).thenReturn(Member.builder().build());
        when(memberAddressService.getMemberAddress(any())).thenReturn(MemberAddress.builder().build());
        when(storeService.getStore(any())).thenReturn(Store.builder().build());
        when(menuService.getMenu(any())).thenReturn(Menu.builder().build());
        when(orderRepository.findAvgDeliveryTimeByType(any())).thenReturn(30L);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // when & then
        assertThatThrownBy(() -> exceptionOrderService.getOrderNew(username, request))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.MENU_OPTION_SERIALIZATION_FAIL.getMessage());
    }

}
