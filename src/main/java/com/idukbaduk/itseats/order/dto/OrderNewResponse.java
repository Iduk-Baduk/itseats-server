package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderNewResponse {

    private int defaultTimeMin;
    private int defaultTimeMax;
    private int onlyOneTimeMin;
    private int onlyOneTimeMax;
    private int orderPrice;
    private int deliveryFee;
    private int discountValue;
    private int totalCost;
}
