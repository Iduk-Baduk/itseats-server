package com.idukbaduk.itseats.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.coupon.entity.MemberCoupon;
import com.idukbaduk.itseats.coupon.error.CouponException;
import com.idukbaduk.itseats.coupon.error.enums.CouponErrorCode;
import com.idukbaduk.itseats.coupon.repository.MemberCouponRepository;
import com.idukbaduk.itseats.coupon.service.CouponPolicyService;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderMenuRepository;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.error.PaymentException;
import com.idukbaduk.itseats.payment.error.enums.PaymentErrorCode;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.entity.StoreImage;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreImageRepository;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String LETTER_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final StoreImageRepository storeImageRepository;
    private final MemberRepository memberRepository;
    private final MemberAddressRepository memberAddressRepository;
    private final PaymentRepository paymentRepository;

    private final ObjectMapper objectMapper;
    private final MemberCouponRepository memberCouponRepository;
    private final CouponPolicyService couponPolicyService;

    @Transactional
    public OrderDetailsResponse getOrderDetails(String username, Long storeId, Long couponId, Integer orderPrice) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Store store = storeRepository.findByStoreId(storeId)
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        int discountValue = 0;
        if (couponId != null) {
            MemberCoupon memberCoupon = memberCouponRepository.findById(couponId)
                    .orElseThrow(() -> new CouponException(CouponErrorCode.COUPON_NOT_FOUND));

            discountValue = couponPolicyService.applyCouponDiscount(memberCoupon, member, orderPrice);
        }

        return buildOrderDetailsResponse(store.getDefaultDeliveryFee(), store.getOnlyOneDeliveryFee(), discountValue);
    }

    private OrderDetailsResponse buildOrderDetailsResponse(int defaultDeliveryFee, int onlyOneDeliveryFee, int discountValue) {
        return OrderDetailsResponse.builder()
                .defaultTimeMin(
                        Optional.ofNullable(orderRepository.findMinDeliveryTimeByType(DeliveryType.DEFAULT.name()))
                                .orElse(30)
                )
                .defaultTimeMax(
                        Optional.ofNullable(orderRepository.findMaxDeliveryTimeByType(DeliveryType.DEFAULT.name()))
                                .orElse(40)
                )
                .onlyOneTimeMin(
                        Optional.ofNullable(orderRepository.findMinDeliveryTimeByType(DeliveryType.ONLY_ONE.name()))
                                .orElse(20)
                )
                .onlyOneTimeMax(
                        Optional.ofNullable(orderRepository.findMaxDeliveryTimeByType(DeliveryType.ONLY_ONE.name()))
                                .orElse(30)
                )
                .defaultDeliveryFee(defaultDeliveryFee)
                .onlyOneDeliveryFee(onlyOneDeliveryFee)
                .discountValue(discountValue)
                .build();
    }

    @Transactional
    public OrderCreateResponse createOrder(String username, OrderCreateRequest request) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        MemberAddress address = memberAddressRepository.findByMemberAndAddressId(member, request.getAddrId())
                .orElseThrow(() -> new MemberAddressException(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND));
        Store store = storeRepository.findByStoreId(request.getStoreId())
                .orElseThrow(() -> new StoreException(StoreErrorCode.STORE_NOT_FOUND));

        Order order = saveOrder(member, store, address, request);
        saveAllOrderMenu(order, request);
        return buildOrderCreateResponse(order);
    }

    private Order saveOrder(Member member, Store store, MemberAddress address, OrderCreateRequest request) {
        Order order = Order.builder()
                .member(member)
                .store(store)
                .orderNumber(getOrderNumber())
                .orderPrice(getOrderPrice(request.getOrderMenus()))
                .orderStatus(OrderStatus.PENDING)
                .deliveryType(DeliveryType.valueOf(request.getDeliveryType()))
                .deliveryEta(LocalDateTime.now()
                        .plusMinutes(getAvgDeliveryTime(request.getDeliveryType())))
                .deliveryFee(getDeliveryFee(store, request.getDeliveryType()))
                .deliveryAddress(address.getMainAddress() + " " + address.getDetailAddress())
                .destinationLocation(address.getLocation())
                .storeLocation(store.getLocation())
                .orderReceivedTime(LocalDateTime.now())
                .cookStartTime(LocalDateTime.now())
                .tossOrderId(generateTossOrderId())
                .build();

        return orderRepository.save(order);
    }

    private String getOrderNumber() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        char firstLetter = LETTER_POOL.charAt(random.nextInt(LETTER_POOL.length()));
        char lastLetter = LETTER_POOL.charAt(random.nextInt(LETTER_POOL.length()));

        return String.format("%c%d%c", firstLetter, timestamp % 10000, lastLetter);
    }

    private String generateTossOrderId() {
        // UUID 생성 후 하이픈 제거하여 32자리 문자열 생성
        return UUID.randomUUID().toString().replace("-", "");
    }

    private int getOrderPrice(List<OrderMenuDTO> orderMenuDtos) {
        return orderMenuDtos.stream()
                .mapToInt(dto -> dto.getMenuTotalPrice() * dto.getQuantity())
                .sum();
    }

    private long getAvgDeliveryTime(String deliveryType) {
        Long avgDeliveryTime = orderRepository.findAvgDeliveryTimeByType(deliveryType);
        return avgDeliveryTime == null ? 30L : avgDeliveryTime;
    }

    private int getDeliveryFee(Store store, String deliveryType) {
        return deliveryType.equals(DeliveryType.DEFAULT.name())
                ? store.getDefaultDeliveryFee()
                : store.getOnlyOneDeliveryFee();
    }

    private void saveAllOrderMenu(Order order, OrderCreateRequest request) {
        List<OrderMenuDTO> orderMenuDtos = request.getOrderMenus();
        List<OrderMenu> orderMenus = new ArrayList<>();
        for (OrderMenuDTO orderMenuDTO : orderMenuDtos) {
            Menu menu = menuRepository.findByMenuIdAndDeletedFalse(orderMenuDTO.getMenuId())
                    .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));
            OrderMenu orderMenu = OrderMenu.builder()
                    .menu(menu)
                    .order(order)
                    .quantity(orderMenuDTO.getQuantity())
                    .price(orderMenuDTO.getMenuTotalPrice())
                    .menuName(menu.getMenuName())
                    .menuOption(convertMenuOptionToJson(orderMenuDTO.getMenuOption()))
                    .build();

            orderMenus.add(orderMenu);
        }

        orderMenuRepository.saveAll(orderMenus);
    }

    private String convertMenuOptionToJson(List<MenuOptionDTO> menuOption) {
        try {
            return objectMapper.writeValueAsString(menuOption);
        } catch (Exception e) {
            throw new OrderException(OrderErrorCode.MENU_OPTION_SERIALIZATION_FAIL);
        }
    }

    private OrderCreateResponse buildOrderCreateResponse(Order order) {
        return OrderCreateResponse.builder()
                .orderId(order.getOrderId())
                .tossOrderId(order.getTossOrderId())
                .build();
    }

    @Transactional
    public OrderHistoryResponse getOrders(String username, String keyword, Pageable pageable) {
        if (username == null || username.trim().isEmpty())
            throw new MemberException(MemberErrorCode.MEMBER_NOT_FOUND);

        Slice<Order> orders = orderRepository.findOrdersByUsernameWithKeyword(username, keyword, pageable);
        return OrderHistoryResponse.builder()
                .orders(orders.stream().map(order -> {
                    List<StoreImage> storeImage = storeImageRepository.findAllByStoreIdOrderByDisplayOrderAsc(
                            order.getStore().getStoreId()
                    );
                    return OrderHistoryDto.of(order, storeImage.isEmpty() ? null : storeImage.get(0));
                }).toList())
                .currentPage(pageable.getPageNumber())
                .hasNext(orders.hasNext())
                .build();
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(String username, Long orderId) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        Order order = orderRepository.findByMemberAndOrderId(member, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        Store store = order.getStore();
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new PaymentException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        return OrderStatusResponse.builder()
                .orderId(orderId)
                .deliveryEta(order.getDeliveryEta().toString())
                .orderStatus(order.getOrderStatus().name())
                .storeName(store.getStoreName())
                .orderNumber(order.getOrderNumber())
                .orderPrice(order.getOrderPrice())
                .orderMenuCount(orderMenuRepository.countOrderMenus(orderId))
                .deliveryAddress(order.getDeliveryAddress())
                .destinationLocation(AddressInfoDTO.builder()
                        .lat(order.getDestinationLocation().getY())
                        .lng(order.getDestinationLocation().getX())
                        .build())
                .storeLocation(AddressInfoDTO.builder()
                        .lat(store.getLocation().getY())
                        .lng(store.getLocation().getX())
                        .build())
                .riderRequest(payment.getRiderRequest())
                .build();
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
    }
}
