package com.idukbaduk.itseats.rider.dto;

import lombok.Builder;

@Builder
public class ReadyOrderResponse{
    private String deliveryType;
    private String storeName;
    private double deliveryDistance;
    private int deliveryFee;
    private String deliveryAddress;
}
