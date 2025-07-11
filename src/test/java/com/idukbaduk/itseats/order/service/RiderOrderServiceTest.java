package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.order.dto.RiderOrderDetailsResponse;
import com.idukbaduk.itseats.order.dto.OrderItemDTO;
import com.idukbaduk.itseats.order.dto.OrderRequestResponse;
import com.idukbaduk.itseats.order.dto.RiderImageResponse;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.RiderImage;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.rider.entity.Rider;
import com.idukbaduk.itseats.rider.error.RiderException;
import com.idukbaduk.itseats.rider.error.enums.RiderErrorCode;
import com.idukbaduk.itseats.rider.repository.RiderRepository;
import com.idukbaduk.itseats.rider.service.RiderService;
import com.idukbaduk.itseats.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RiderOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private RiderRepository riderRepository;
    @Mock
    private RiderImageService riderImageService;
    @Mock
    private RiderService riderService;

    @InjectMocks
    private RiderOrderService riderOrderService;

    private final String username = "testuser";
    private Member member;
    private Member customer;
    private Order order;
    private Rider rider;
    private Store store;
    private OrderMenu menu1;
    private OrderMenu menu2;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();

        customer = Member.builder()
                .memberId(2L)
                .phone("010-0000-0000")
                .build();

        rider = Rider.builder()
                .riderId(1L)
                .member(member)
                .location(GeoUtil.toPoint(127.0000, 37.7600))
                .build();

        store = Store.builder()
                .storeId(1L)
                .storePhone("010-1111-2222")
                .storeAddress("서울시 구름구 구름동100번길 10 1층")
                .location(GeoUtil.toPoint(126.0005, 37.7601))
                .build();

        menu1 = OrderMenu.builder()
                .orderMenuId(1L)
                .order(order)
                .menuName("테스트1")
                .price(10000)
                .quantity(2)
                .menuOption("\"optionName\": \"테스트 옵션1\",\"optionPrice\": 5000}")
                .build();

        menu2 = OrderMenu.builder()
                .orderMenuId(2L)
                .order(order)
                .menuName("테스트2")
                .price(10000)
                .quantity(1)
                .menuOption("")
                .build();

        order = Order.builder()
                .orderId(1L)
                .member(customer)
                .rider(rider)
                .store(store)
                .orderNumber("A1234B")
                .orderPrice(45000)
                .orderStatus(OrderStatus.COOKING)
                .orderMenus(List.of(menu1, menu2))
                .build();

        setCreatedAt();
    }

    private void setCreatedAt() {
        try {
            Field field = Order.class.getSuperclass().getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(order, LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("createdAt 필드 설정 실패", e);
        }
    }

    @Test
    @DisplayName("라이더 - 주문 정보 조회 성공")
    void getOrderDetails_success() {
        // given
        Payment payment = Payment.builder()
                .paymentId(1L)
                .storeRequest("맛있게 만들어주세요")
                .riderRequest("문 앞에 두고 가주세요")
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrder(order)).thenReturn(Optional.of(payment));

        // when
        RiderOrderDetailsResponse response = riderOrderService.getOrderDetails(username, 1L);

        // then
        assertThat(response.getOrderId()).isEqualTo(order.getOrderId());
        assertThat(response.getOrderNumber()).isEqualTo(order.getOrderNumber());
        assertThat(response.getOrderStatus()).isEqualTo(order.getOrderStatus().name());
        assertThat(response.getTotalPrice()).isEqualTo(order.getOrderPrice());
        assertThat(response.getOrderTime()).isEqualTo(order.getCreatedAt().toString());
        assertThat(response.getOrderItems()).hasSize(2);
        assertThat(response.getStorePhone()).isEqualTo(store.getStorePhone());
        assertThat(response.getMemberPhone()).isEqualTo(customer.getPhone());
        assertThat(response.getStoreRequest()).isEqualTo(payment.getStoreRequest());
        assertThat(response.getRiderRequest()).isEqualTo(payment.getRiderRequest());

        OrderItemDTO orderItemDto = response.getOrderItems().get(0);
        assertThat(orderItemDto.getMenuName()).isEqualTo(menu1.getMenuName());
        assertThat(orderItemDto.getQuantity()).isEqualTo(menu1.getQuantity());
        assertThat(orderItemDto.getMenuPrice()).isEqualTo(menu1.getPrice());
        assertThat(orderItemDto.getOptions()).isEqualTo(menu1.getMenuOption());
    }

    @Test
    @DisplayName("존재하지 않는 라이더 조회시 예외 발생")
    void getOrderDetails_notExistRider(){
        // given
        when(riderRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderOrderService.getOrderDetails(username, 1L))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("배차되지 않은 주문을 조회하는 경우 예외 발생")
    void getOrderDetails_notAssignOrder() {
        // given
        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderOrderService.getOrderDetails(username, 1L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문 상태를 배차 완료 상태로 변경에 성공")
    void acceptDelivery_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.COOKED)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        riderOrderService.acceptDelivery(username, 1L);

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

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        assertThatThrownBy(() -> riderOrderService.acceptDelivery(username, 1L))
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

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // when
        assertThatThrownBy(() -> riderOrderService.acceptDelivery(username, 1L))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_ALREADY_ASSIGNED.getMessage());
    }

    @Test
    @DisplayName("주문 상태를 배달 완료 상태로 변경에 성공")
    void updateOrderStatus_delivered_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.DELIVERING)
                .rider(rider)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));

        // when
        riderOrderService.updateOrderStatus(username, 1L, OrderStatus.DELIVERED);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERED);
    }

    @Test
    @DisplayName("주문이 직전 단계가 아닌 경우 주문 상태를 변경하면 예외 발생")
    void updateOrderStatus_deliveredFail() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.COOKING)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));

        // when
        assertThatThrownBy(() -> riderOrderService.updateOrderStatus(username, 1L, OrderStatus.DELIVERED))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_STATUS_UPDATE_FAIL.getMessage());
    }

    @Test
    @DisplayName("배차되지 않은 주문의 상태를 변경하는 경우 예외 발생")
    void updateOrderStatus_getOrderFail() {
        // given
        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderOrderService.updateOrderStatus(username, 1L, OrderStatus.DELIVERED))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주문 상태를 매장 도착 상태로 변경에 성공")
    void updateOrderStatus_arrived_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.RIDER_READY)
                .rider(rider)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));

        // when
        riderOrderService.updateOrderStatus(username, 1L, OrderStatus.ARRIVED);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.ARRIVED);
    }

    @Test
    @DisplayName("주문 상태를 배달 중 상태로 변경에 성공")
    void updateOrderStatus_delivering_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .orderStatus(OrderStatus.ARRIVED)
                .rider(rider)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));

        // when
        riderOrderService.updateOrderStatus(username, 1L, OrderStatus.DELIVERING);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.DELIVERING);
    }

    @Test
    @DisplayName("라이더 배달 완료 사진 업로드 성공")
    void uploadRiderImage_success() {
        // given
        MultipartFile multipartFile = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test".getBytes()
        );
        RiderImage riderImage = RiderImage.builder()
                .imageUrl("https://example.com/test.jpg")
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findByRiderAndOrderId(rider, 1L)).thenReturn(Optional.of(order));
        when(riderImageService.saveRiderImage(rider, order, multipartFile)).thenReturn(riderImage);

        // when
        RiderImageResponse response = riderOrderService.uploadRiderImage(username, 1L, multipartFile);

        // then
        assertThat(response.getImage()).isEqualTo(riderImage.getImageUrl());
    }

    @Test
    @DisplayName("배달 완료 이미지 파일이 null일 경우 예외 발생")
    void uploadRiderImage_riderImageNull() {
        assertThatThrownBy(() -> riderOrderService.uploadRiderImage(username, 1L, null))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.REQUIRED_RIDER_IMAGE.getMessage());
    }

    @Test
    @DisplayName("배달 완료 이미지 파일이 비어있는 경우 예외 발생")
    void uploadRiderImage_riderImageEmpty() {
        // given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "image", "", "image/jpeg", new byte[0]
        );

        // when & then
        assertThatThrownBy(() -> riderOrderService.uploadRiderImage(username, 1L, emptyFile))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.REQUIRED_RIDER_IMAGE.getMessage());
    }

    @Test
    @DisplayName("라이더 근처 배달 요청 조회 성공")
    void getOrderRequest_success() {
        // given
        Order order = Order.builder()
                .orderId(1L)
                .member(customer)
                .store(store)
                .orderNumber("A1234B")
                .orderPrice(45000)
                .deliveryType(DeliveryType.DEFAULT)
                .deliveryFee(3000)
                .orderStatus(OrderStatus.COOKED)
                .orderMenus(List.of(menu1, menu2))
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findCookedOrderByRiderLocation(rider.getLocation().getY(), rider.getLocation().getX()))
                .thenReturn(Optional.of(order));
        doNothing().when(riderService).createRiderAssignment(any(Rider.class), any(Order.class));

        // when
        OrderRequestResponse response = riderOrderService.getOrderRequest(username);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getOrderId()).isEqualTo(order.getOrderId());
        assertThat(response.getDeliveryType()).isEqualTo(order.getDeliveryType().name());
        assertThat(response.getDeliveryFee()).isEqualTo(order.getDeliveryFee());
        assertThat(response.getStoreName()).isEqualTo(order.getStore().getStoreName());
        assertThat(response.getMyLocation().getLat()).isEqualTo(rider.getLocation().getY());
        assertThat(response.getMyLocation().getLng()).isEqualTo(rider.getLocation().getX());
        assertThat(response.getStoreLocation().getLat()).isEqualTo(store.getLocation().getY());
        assertThat(response.getStoreLocation().getLng()).isEqualTo(store.getLocation().getX());
        assertThat(response.getStoreAddress()).isEqualTo(store.getStoreAddress());
    }

    @Test
    @DisplayName("조건에 맞는 주문이 없는 경우 예외 발생")
    void getOrderRequest_notExistOrder() {
        // given
        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(rider));
        when(orderRepository.findCookedOrderByRiderLocation(rider.getLocation().getY(), rider.getLocation().getX()))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> riderOrderService.getOrderRequest(username))
                .isInstanceOf(OrderException.class)
                .hasMessageContaining(OrderErrorCode.ORDER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("라이더 위치 정보가 없으면 예외 발생")
    void getOrderRequest_notExistRiderLocation() {
        // given
        Rider noLocationRider = Rider.builder()
                .riderId(1L)
                .member(member)
                .build();

        when(riderRepository.findByUsername(username)).thenReturn(Optional.of(noLocationRider));

        // when & then
        assertThatThrownBy(() -> riderOrderService.getOrderRequest(username))
                .isInstanceOf(RiderException.class)
                .hasMessageContaining(RiderErrorCode.RIDER_LOCATION_NOT_FOUND.getMessage());
    }
}
