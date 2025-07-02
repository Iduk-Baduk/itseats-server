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
    String orderStatus;
    Integer orderPrice;
    String deliveryAddress;
    String deliveryRequest;
    String menuSummary;

    public static OrderHistoryDto of(Order order) {
        if (order == null)
            throw new IllegalArgumentException("Order cannot be null");
        if (order.getStore() == null)
            throw new IllegalStateException("Order must have an associated store");

        return builder()
                .orderId(order.getOrderId())
                .storeId(order.getStore().getStoreId())
                .orderNumber(order.getOrderNumber())
                .storeName(order.getStore().getStoreName())
                .createdAt(order.getCreatedAt())
                .orderStatus(order.getOrderStatus().toString())
                .orderPrice(order.getOrderPrice())
                .deliveryAddress(order.getDeliveryAddress())
                .deliveryRequest("") // TODO: 배송 요청사항 필드 구현 필요
                .menuSummary(
                        order.getOrderMenus() != null ?
                        order.getOrderMenus().stream()
                                .map(OrderMenu::getMenuName)
                                .collect(Collectors.joining(", ")) :
                        "메뉴 정보 없음"
                )
                .build();
    }
}
