package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.OrderReceptionDTO;
import com.idukbaduk.itseats.order.dto.OrderReceptionResponse;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import com.idukbaduk.itseats.payment.entity.Payment;
import com.idukbaduk.itseats.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerOrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

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
                .mapToInt(OrderMenu::getPrice)
                .sum();

        Optional<Payment> optionalPayment = paymentRepository.findByOrder(order);
        String customerRequest = optionalPayment
                .map(payment -> combineRequests(payment.getStoreRequest(), payment.getRiderRequest()))
                .orElse("");

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

    private String combineRequests(String storeRequest, String riderRequest) {
        if (storeRequest == null && riderRequest == null) return "";
        if (storeRequest == null) return riderRequest;
        if (riderRequest == null) return storeRequest;
        return storeRequest + " " + riderRequest;
    }
}
