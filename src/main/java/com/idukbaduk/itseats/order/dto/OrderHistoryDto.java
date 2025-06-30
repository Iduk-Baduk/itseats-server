package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

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
}
