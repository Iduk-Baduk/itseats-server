package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderDetailResponse {
    private Long orderId;
    private String orderNumber;
    private String memberName;
    private String orderStatus;
    private String orderTime;
    private int totalPrice;
    private List<OrderMenuItemDTO> menuItems;
}
