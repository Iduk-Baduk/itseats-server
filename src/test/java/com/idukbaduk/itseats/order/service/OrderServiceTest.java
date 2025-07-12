package com.idukbaduk.itseats.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.coupon.entity.Coupon;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.entity.enums.CouponType;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.coupon.service.CouponPolicyService;
import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderMenuRepository;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMenuRepository orderMenuRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreImageRepository storeImageRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private MemberAddressRepository memberAddressRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private MemberCouponRepository memberCouponRepository;

    @Mock
    private CouponPolicyService couponPolicyService;

    @InjectMocks
    private OrderService orderService;

    private final String username = "testuser";
    private Member member;
    private Coupon coupon;
    private MemberCoupon memberCoupon;
    private Store store;
    private MemberAddress address;
    private OrderCreateRequest orderCreateRequest;
    private Order order;

    @BeforeEach
    void setup() {
        orderService = new OrderService(
                orderRepository,
                orderMenuRepository,
                menuRepository,
                storeRepository,
                storeImageRepository,
                memberRepository,
                memberAddressRepository,
                paymentRepository,
                new ObjectMapper(),
                memberCouponRepository,
                couponPolicyService
        );

        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();

        coupon = Coupon.builder()
                .couponId(1L)
                .couponType(CouponType.FIXED)
                .discountValue(2000)
                .minPrice(5000)
                .build();

        memberCoupon = MemberCoupon.builder()
                .memberCouponId(1L)
                .member(member)
                .coupon(coupon)
                .isUsed(false)
                .validDate(LocalDateTime.now().plusDays(3))
                .build();

        store = Store.builder()
                .storeId(1L)
                .storeName("테스트 구름점")
                .defaultDeliveryFee(3000)
                .onlyOneDeliveryFee(3500)
                .location(GeoUtil.toPoint(127.0, 37.5))
                .build();

        address = MemberAddress.builder()
                .addressId(1L)
                .mainAddress("서울시 구름구 구름로100번길 10")
                .detailAddress("100호")
                .build();

        order = Order.builder()
                .orderId(1L)
                .store(store)
                .deliveryEta(LocalDateTime.of(2025, 6, 20, 12, 0, 0))
                .orderStatus(OrderStatus.COOKING)
                .orderNumber("A1234B")
                .orderPrice(10000)
                .deliveryAddress("서울시 구름구 구름로100번길 10 100호")
                .destinationLocation(GeoUtil.toPoint(126.9, 37.4))
                .storeLocation(GeoUtil.toPoint(127.0, 37.5))
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

        orderCreateRequest = OrderCreateRequest.builder()
                .addrId(1L)
                .storeId(1L)
                .deliveryType(DeliveryType.DEFAULT.name())
                .orderMenus(List.of(orderMenuDTO1, orderMenuDTO2))
                .build();
    }

    @Test
    @DisplayName("배달 정보 상세 조회 성공")
    void getOrderDetails_success() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(storeRepository.findByStoreId(1L)).thenReturn(Optional.ofNullable(store));

        when(orderRepository.findMinDeliveryTimeByType(DeliveryType.DEFAULT.name())).thenReturn(20);
        when(orderRepository.findMaxDeliveryTimeByType(DeliveryType.DEFAULT.name())).thenReturn(40);
        when(orderRepository.findMinDeliveryTimeByType(DeliveryType.ONLY_ONE.name())).thenReturn(25);
        when(orderRepository.findMaxDeliveryTimeByType(DeliveryType.ONLY_ONE.name())).thenReturn(45);
        when(memberCouponRepository.findById(1L)).thenReturn(Optional.ofNullable(memberCoupon));
        when(couponPolicyService.applyCouponDiscount(memberCoupon, member, 10000)).thenReturn(2000);

        // when
        OrderDetailsResponse response = orderService.getOrderDetails(username, 1L, 1L, 10000);

        // then
        assertThat(response.getDefaultDeliveryFee()).isEqualTo(3000);
        assertThat(response.getOnlyOneDeliveryFee()).isEqualTo(3500);
        assertThat(response.getDefaultTimeMin()).isEqualTo(20);
        assertThat(response.getOnlyOneTimeMax()).isEqualTo(45);
        assertThat(response.getDiscountValue()).isEqualTo(2000);
    }

    @Test
    @DisplayName("쿠폰이 없거나 본인 소유가 아니면 예외 발생")
    void getOrderDetails_couponNotFound() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(storeRepository.findByStoreId(1L)).thenReturn(Optional.ofNullable(store));
        when(memberCouponRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetails(username, 1L, 1L, 10000))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.COUPON_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 사용된 쿠폰이면 예외 발생")
    void getOrderDetails_couponAlreadyUsed() {
        // given
        MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberCouponId(1L)
                .member(member)
                .coupon(coupon)
                .isUsed(true)
                .validDate(LocalDateTime.now().plusDays(3))
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(storeRepository.findByStoreId(1L)).thenReturn(Optional.ofNullable(store));
        when(memberCouponRepository.findById(1L)).thenReturn(Optional.of(memberCoupon));

        doThrow(new CouponException(CouponErrorCode.COUPON_ALREADY_USED))
                .when(couponPolicyService).applyCouponDiscount(memberCoupon, member, 7000);

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetails(username, 1L, 1L, 7000))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.COUPON_ALREADY_USED.getMessage());
    }

    @Test
    @DisplayName("유효기간이 지난 쿠폰이면 예외 발생")
    void getOrderDetails_couponExpired() {
        // given
        MemberCoupon memberCoupon = MemberCoupon.builder()
                .memberCouponId(1L)
                .member(member)
                .coupon(coupon)
                .isUsed(false)
                .validDate(LocalDateTime.now().minusDays(1))
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(storeRepository.findByStoreId(1L)).thenReturn(Optional.ofNullable(store));
        when(memberCouponRepository.findById(1L)).thenReturn(Optional.of(memberCoupon));

        doThrow(new CouponException(CouponErrorCode.COUPON_EXPIRED))
                .when(couponPolicyService).applyCouponDiscount(memberCoupon, member, 7000);

        // when & then
        assertThatThrownBy(() -> orderService.getOrderDetails(username, 1L, 1L, 7000))
                .isInstanceOf(CouponException.class)
                .hasMessageContaining(CouponErrorCode.COUPON_EXPIRED.getMessage());
    }

    @Test
    @DisplayName("주문 정보가 성공적으로 저장")
    void createOrder_success() {
        // given
        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(memberAddressRepository.findByMemberAndAddressId(member, 1L)).thenReturn(Optional.of(address));
        when(storeRepository.findByStoreId(1L)).thenReturn(Optional.ofNullable(store));
        when(menuRepository.findById(1L)).thenReturn(Optional.of(new Menu()));
        when(menuRepository.findById(2L)).thenReturn(Optional.of(new Menu()));

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // when
        OrderCreateResponse response = orderService.createOrder(username, orderCreateRequest);

        // then
        verify(orderMenuRepository).saveAll(anyList());
    }

    @Test
    @DisplayName("ObjectMapper가 null일 때 menuOption 직렬화 실패 예외 발생")
    void createOrder_serializationException() {
        // given
        OrderService exceptionOrderService = new OrderService(
                orderRepository,
                orderMenuRepository,
                menuRepository,
                storeRepository,
                storeImageRepository,
                memberRepository,
                memberAddressRepository,
                paymentRepository,
                null,
                memberCouponRepository,
                couponPolicyService
        );

        when(memberRepository.findByUsername(any())).thenReturn(Optional.ofNullable(member));
        when(memberAddressRepository.findByMemberAndAddressId(any(), any()))
                .thenReturn(Optional.ofNullable(address));
        when(storeRepository.findByStoreId(any())).thenReturn(Optional.ofNullable(store));
        when(menuRepository.findById(any())).thenReturn(Optional.ofNullable(Menu.builder().build()));
        when(orderRepository.findAvgDeliveryTimeByType(any())).thenReturn(30L);
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));

        // when & then
        assertThatThrownBy(() -> exceptionOrderService.createOrder(username, orderCreateRequest))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.MENU_OPTION_SERIALIZATION_FAIL.getMessage());
    }

    @Test
    @DisplayName("주문 현황 조회 성공")
    void getOrderStatus_success() {
        //given
        Payment payment = Payment.builder()
                .riderRequest("조심히 와주세요")
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.ofNullable(member));
        when(orderRepository.findByMemberAndOrderId(member, 1L)).thenReturn(Optional.of(order));
        when(orderMenuRepository.countOrderMenus(1L)).thenReturn(2L);
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.ofNullable(payment));

        // when
        OrderStatusResponse response = orderService.getOrderStatus(username, 1L);

        // then
        assertThat(response.getDeliveryEta()).isEqualTo(order.getDeliveryEta().toString());
        assertThat(response.getOrderStatus()).isEqualTo(order.getOrderStatus().name());
        assertThat(response.getStoreName()).isEqualTo(order.getStore().getStoreName());
        assertThat(response.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(response.getOrderPrice()).isEqualTo(order.getOrderPrice());
        assertThat(response.getDeliveryAddress()).isEqualTo(order.getDeliveryAddress());
        Assertions.assertNotNull(payment);
        assertThat(response.getRiderRequest()).isEqualTo(payment.getRiderRequest());

        AddressInfoDTO destinationLocationResponse = response.getDestinationLocation();
        Point destinationLocation = order.getDestinationLocation();
        assertThat(destinationLocationResponse.getLat()).isEqualTo(destinationLocation.getY());
        assertThat(destinationLocationResponse.getLng()).isEqualTo(destinationLocation.getX());

        AddressInfoDTO storeLocationResponse = response.getStoreLocation();
        Point storeLocation = order.getStoreLocation();
        assertThat(storeLocationResponse.getLat()).isEqualTo(storeLocation.getY());
        assertThat(storeLocationResponse.getLng()).isEqualTo(storeLocation.getX());
    }

    @Test
    @DisplayName("존재하지 않는 주문 조회시 예외 발생")
    void getOrderStatus_notExist() {
        // given
        when(memberRepository.findByUsername(any())).thenReturn(Optional.ofNullable(member));
        when(orderRepository.findByMemberAndOrderId(member, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrderStatus(username, 1L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문 정보를 성공적으로 반환")
    void getOrder_success() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        Order result = orderService.getOrder(1L);

        // then
        assertThat(result).isEqualTo(order);
    }

    @Test
    @DisplayName("존재하지 않는 메뉴 조회시 예외 발생")
    void getOrder_notExist() {
        // given
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> orderService.getOrder(1L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("과거 주문내역 조회 성공")
    void getOrders_success() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 1);
        List<OrderMenu> orderMenuList = List.of(
                OrderMenu.builder().menuName("양념치킨").build(),
                OrderMenu.builder().menuName("간장치킨").build()
        );
        List<Order> orderList = List.of(
                Order.builder()
                        .orderId(1L)
                        .store(Store.builder().storeId(1L).storeName("치킨집").build())
                        .orderStatus(OrderStatus.COOKING)
                        .orderMenus(orderMenuList)
                        .build()
        );
        Slice<Order> orders = new SliceImpl<Order>(orderList, pageRequest, true);

        when(orderRepository.findOrdersByUsernameWithKeyword(anyString(), anyString(), eq(pageRequest)))
                .thenReturn(orders);

        // when
        OrderHistoryResponse data = orderService.getOrders("username", "keyword", pageRequest);

        // then
        assertThat(data).isNotNull();
        assertThat(data.getOrders()).hasSize(1)
                .extracting("orderId", "storeId", "storeName", "orderStatus", "menuSummary")
                .containsExactly(tuple(1L, 1L, "치킨집", "COOKING", "양념치킨, 간장치킨"));
    }
}
