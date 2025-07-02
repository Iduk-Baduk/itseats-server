package com.idukbaduk.itseats.order.dto;

import com.idukbaduk.itseats.order.entity.Order;
import com.idukbaduk.itseats.order.entity.OrderMenu;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Getter
@Builder
public class OrderHistoryDto {
    Long orderId;
    Long storeId;
    String orderNumber;
    String storeName;
    LocalDateTime createdAt;
    String status;
    Integer orderPrice;
    String deliveryAddress;
    String deliveryRequest;
    String menuSummary;

    public static OrderHistoryDto of(Order order) {
        return builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getStoreId())
                .orderNumber(order.getOrderNumber())
                .storeName(order.getStore().getStoreName())
                .createdAt(order.getCreatedAt())
                .status(order.getOrderStatus().toString())
                .orderPrice(order.getOrderPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryRequest("구현 예정")
                .menuSummary(
                        order.getOrderMenus().stream()
                                .map(OrderMenu::getMenuName)
                                .collect(Collectors.joining(", "))
                )
                .build();
    }
}
