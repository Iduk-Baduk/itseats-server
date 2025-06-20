package com.idukbaduk.itseats.order.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.service.MemberAddressService;
import com.idukbaduk.itseats.menu.service.MenuService;
import com.idukbaduk.itseats.order.dto.AddressInfoDTO;
import com.idukbaduk.itseats.order.dto.MenuOptionDTO;
import com.idukbaduk.itseats.order.dto.OrderMenuDTO;
import com.idukbaduk.itseats.order.dto.OrderNewRequest;
import com.idukbaduk.itseats.order.dto.OrderNewResponse;
import com.idukbaduk.itseats.order.dto.OrderStatusResponse;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderMenuRepository;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.order.entity.enums.DeliveryType;
import com.idukbaduk.itseats.payment.service.PaymentService;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String LETTER_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private final OrderRepository orderRepository;
    private final OrderMenuRepository orderMenuRepository;

    private final MenuService menuService;
    private final StoreService storeService;
    private final MemberService memberService;
    private final MemberAddressService memberAddressService;
    private final PaymentService paymentService;

    private final ObjectMapper objectMapper;

    @Transactional
    public OrderNewResponse getOrderNew(String username, OrderNewRequest orderNewRequest) {
        Member member = memberService.getMemberByUsername(username);
        MemberAddress address = memberAddressService.getMemberAddress(member, orderNewRequest.getAddrId());
        Store store = storeService.getStore(member, orderNewRequest.getStoreId());

        Order order = saveOrder(member, store, address, orderNewRequest);
        saveAllOrderMenu(order, orderNewRequest);

        int orderPrice = getOrderPrice(orderNewRequest.getOrderMenus());
        int deliveryFee = getDevliveryFee(store, orderNewRequest.getDeliveryType());

        return OrderNewResponse.builder()
                .defaultTimeMin(orderRepository.findMinDeliveryTimeByType(DeliveryType.DEFAULT.name()))
                .defaultTimeMax(orderRepository.findMaxDeliveryTimeByType(DeliveryType.DEFAULT.name()))
                .onlyOneTimeMin(orderRepository.findMinDeliveryTimeByType(DeliveryType.ONLY_ONE.name()))
                .onlyOneTimeMax(orderRepository.findMaxDeliveryTimeByType(DeliveryType.ONLY_ONE.name()))
                .orderPrice(orderPrice)
                .deliveryFee(deliveryFee)
                // 쿠폰 관련 로직은 추후 구현
                .discountValue(0)
                .totalCost(orderPrice + deliveryFee)
                .build();
    }

    private Order saveOrder(Member member, Store store, MemberAddress address, OrderNewRequest orderNewRequest) {
        Order order = Order.builder()
                .member(member)
                .store(store)
                .orderNumber(getOrderNumber())
                .orderPrice(getOrderPrice(orderNewRequest.getOrderMenus()))
                .orderStatus(OrderStatus.WAITING)
                .deliveryType(DeliveryType.valueOf(orderNewRequest.getDeliveryType()))
                .deliveryEta(LocalDateTime.now()
                        .plusMinutes(orderRepository.findAvgDeliveryTimeByType(orderNewRequest.getDeliveryType())))
                .deliveryFee(getDevliveryFee(store, orderNewRequest.getDeliveryType()))
                .deliveryAddress(address.getMainAddress() + " " + address.getDetailAddress())
                .destinationLocation(address.getLocation())
                .storeLocation(store.getLocation())
                .build();
        orderRepository.save(order);
        return order;
    }

    private String getOrderNumber() {
        long timestamp = System.currentTimeMillis();
        Random random = new Random();
        char firstLetter = LETTER_POOL.charAt(random.nextInt(LETTER_POOL.length()));
        char lastLetter = LETTER_POOL.charAt(random.nextInt(LETTER_POOL.length()));

        return String.format("%c%d%c", firstLetter, timestamp % 10000, lastLetter);
    }

    private int getOrderPrice(List<OrderMenuDTO> orderMenuDtos) {
        return orderMenuDtos.stream()
                .mapToInt(dto -> dto.getMenuTotalPrice() * dto.getQuantity())
                .sum();
    }

    private int getDevliveryFee(Store store, String deliveryType) {
        return deliveryType.equals(DeliveryType.DEFAULT.name())
                ? store.getDefaultDeliveryFee()
                : store.getOnlyOneDeliveryFee();
    }

    private void saveAllOrderMenu(Order order, OrderNewRequest orderNewRequest) {
        List<OrderMenuDTO> orderMenuDtos = orderNewRequest.getOrderMenus();
        List<OrderMenu> orderMenus = new ArrayList<>();
        for (OrderMenuDTO orderMenuDTO : orderMenuDtos) {
            OrderMenu orderMenu = OrderMenu.builder()
                    .menu(menuService.getMenu(orderMenuDTO.getMenuId()))
                    .order(order)
                    .quantity(orderMenuDTO.getQuantity())
                    .price(orderMenuDTO.getMenuTotalPrice())
                    .menuName(orderMenuDTO.getMenuName())
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

    public OrderStatusResponse getOrderStatus(String username, Long orderId) {
        Member member = memberService.getMemberByUsername(username);
        Order order = orderRepository.findByMemberAndOrderId(member, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));
        Store store = order.getStore();

        return OrderStatusResponse.builder()
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
                .riderReqeust(paymentService.getPaymentByOrder(order).getRiderRequest())
                .build();
    }
}
