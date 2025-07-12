package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderDetailsResponse {

    private int defaultTimeMin;
    private int defaultTimeMax;
    private int onlyOneTimeMin;
    private int onlyOneTimeMax;
    private int defaultDeliveryFee;
    private int onlyOneDeliveryFee;
    private int discountValue;
}
