package com.idukbaduk.itseats.order.service;

import com.idukbaduk.itseats.order.dto.OrderDetailResponse;
import com.idukbaduk.itseats.order.dto.OrderMenuItemDTO;
import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import com.idukbaduk.itseats.order.error.OrderException;
import com.idukbaduk.itseats.order.error.enums.OrderErrorCode;
import com.idukbaduk.itseats.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerOrderService {

    private final OrderRepository orderRepository;

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
}
