package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderReceptionResponse {
    private Long orderId;
    private String orderNumber;
    private String orderTime;
    private int menuCount;
    private int totalPrice;
    private List<OrderReceptionDTO> menuItems;
    private String orderStatus;
    private String customerRequest;
    private String riderPhone;
}
