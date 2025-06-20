package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderStatusResponse {

    private String deliveryEta;
    private String orderStatus;
    private String storeName;
    private String orderNumber;
    private int orderPrice;
    private Long orderMenuCount;
    private String deliveryAddress;
    private AddressInfoDTO destinationLocation;
    private AddressInfoDTO storeLocation;
    private String riderRequest;
}
