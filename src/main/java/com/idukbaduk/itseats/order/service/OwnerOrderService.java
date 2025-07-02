package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.*;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.entity.enums.OrderStatus;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
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
    public OrderDetailResponse getOrderDetail(Long orderId) {

        Order order = orderRepository.findDetailById(orderId)
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

        return OrderDetailResponse.builder()
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
    public List<OrderReceptionResponse> getOrders(Long storeId) {
        List<Order> orders = orderRepository.findAllWithMenusByStoreId(storeId);

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
    public OrderRejectResponse rejectOrder(Long orderId, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.reject(reason);

        return new OrderRejectResponse(true, reason);
    }
  
    public OrderAcceptResponse acceptOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.ACCEPTED);

        return new OrderAcceptResponse(true);
    }

    @Transactional
    public OrderCookedResponse markAsCooked(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        order.updateStatus(OrderStatus.COOKED);

        return new OrderCookedResponse(true);
    }

    @Transactional
    public CookTimeResponse setCookTime(Long orderId, int cookTime) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND));

        if (order.getOrderStatus() == OrderStatus.COMPLETED || order.getOrderStatus() == OrderStatus.REJECTED) {
            throw new OrderException(OrderErrorCode.INVALID_ORDER_STATUS);
        }

        LocalDateTime deliveryEta = LocalDateTime.now().plusMinutes(cookTime);
        order.updateDeliveryEta(deliveryEta);

        String etaStr = deliveryEta.format(DateTimeFormatter.ofPattern("MM.dd HH:mm"));

        return CookTimeResponse.builder()
                .orderId(order.getOrderId())
                .deliveryEta(etaStr)
                .build();
    }
}
