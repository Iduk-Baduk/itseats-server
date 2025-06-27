package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class OrderRequestResponse {

    private Long orderId;
    private String deliveryType;
    private String storeName;
    private AddressInfoDTO myLocation;
    private AddressInfoDTO storeLocation;
    private int deliveryFee;
    private String storeAddress;
    private LocalDateTime validTime;
}
