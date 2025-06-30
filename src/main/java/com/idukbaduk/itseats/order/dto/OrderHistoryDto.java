package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderHistoryDto {
    Long orderId;
    String orderNumber;
    Long storeId;
    String storeName;
    LocalDateTime createdAt;
    String status;
    Integer orderPrice;
    String menuSummary;
}
