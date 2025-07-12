package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateResponse {

    private Long orderId;
    private int orderPrice;
    private int discountValue;
    private int deliveryFee;
    private int totalCost;
}
