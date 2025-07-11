package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerOrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(readOnly = true)
    public OwnerOrderDetailsResponse getOrderDetail(String username, Long orderId) {
        Order order = orderRepository.findDetailByStoreUsernameAndId(username, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        List<OrderMenuItemDTO> menuItems = order.getOrderMenus().stream()
                .map(om -> OrderMenuItemDTO.builder()
                        .menuId(om.getMenu().getMenuId())
                        .menuName(om.getMenuName())
                        .quantity(om.getQuantity())
                        .menuPrice(om.getPrice())
                        .options(parseOptions(om.getMenuOption()))
                        .build())
                .toList();

        int totalPrice = order.getOrderMenus().stream()
                .mapToInt(om -> om.getPrice() * om.getQuantity())
                .sum();

        return OwnerOrderDetailsResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .memberName(order.getMember().getName())
                .orderStatus(order.getOrderStatus().name())
                .orderTime(order.getOrderReceivedTime().toString())
                .totalPrice(totalPrice)
                .menuItems(menuItems)
                .build();
    }

    private List<String> parseOptions(String menuOption) {
        if (menuOption == null || menuOption.trim().isEmpty()) return List.of();
        return Arrays.stream(menuOption.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
  
    @Transactional(readOnly = true)
    public List<OrderReceptionResponse> getOrders(String username, Long storeId) {
        List<Order> orders = orderRepository.findAllWithMenusByStoreUsernameAndStoreId(username, storeId);

        return orders.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private OrderReceptionResponse convertToResponse(Order order) {
        List<OrderReceptionDTO> menuItems = order.getOrderMenus().stream()
                .map(orderMenu -> OrderReceptionDTO.builder()
                        .menuName(orderMenu.getMenuName())
                        .quantity(orderMenu.getQuantity())
                        .price(orderMenu.getPrice())
                        .menuOption(orderMenu.getMenuOption())
                        .build())
                .collect(Collectors.toList());

        int menuCount = order.getOrderMenus().stream()
                .mapToInt(OrderMenu::getQuantity)
                .sum();

        int totalPrice = order.getOrderMenus().stream()
                .mapToInt(orderMenu -> orderMenu.getPrice() * orderMenu.getQuantity())
                .sum();

        Optional<Payment> optionalPayment = paymentRepository.findByOrder(order);
        String customerRequest = optionalPayment.map(Payment::getStoreRequest).orElse("");

        String riderPhone = null;
        if (order.getRider() != null && order.getRider().getMember() != null) {
            riderPhone = order.getRider().getMember().getPhone();
        }

        return OrderReceptionResponse.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .orderTime(order.getOrderReceivedTime().toString())
                .menuCount(menuCount)
                .totalPrice(totalPrice)
                .menuItems(menuItems)
                .orderStatus(order.getOrderStatus().name())
                .customerRequest(customerRequest)
                .riderPhone(riderPhone)
                .build();
    }

    @Transactional
    public OrderRejectResponse rejectOrder(String username, Long orderId, String reason) {
        Order order = orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.reject(reason);

        return new OrderRejectResponse(true, reason);
    }

    @Transactional
    public OrderAcceptResponse acceptOrder(String username, Long orderId) {
        Order order = orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.ACCEPTED);
        order.updateOrderReceivedTime(LocalDateTime.now());

        return new OrderAcceptResponse(true);
    }

    @Transactional
    public OrderCookedResponse markAsCooked(String username, Long orderId) {
        Order order = orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.COOKED);

        return new OrderCookedResponse(true);
    }

    @Transactional
    public CookTimeResponse setCookTime(String username, Long orderId, int cookTime) {
        Order order = orderRepository.findByStoreMemberUsernameAndOrderId(username, orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.COOKING);
        order.updateCookStartTime(LocalDateTime.now());

        LocalDateTime deliveryEta = LocalDateTime.now().plusMinutes(cookTime);
        order.updateDeliveryEta(deliveryEta);

        String etaStr = deliveryEta.format(DateTimeFormatter.ofPattern("MM.dd HH:mm"));

        return CookTimeResponse.builder()
                .orderId(order.getOrderId())
                .deliveryEta(etaStr)
                .build();
    }
}
