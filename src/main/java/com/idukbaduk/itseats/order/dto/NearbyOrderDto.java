package com.idukbaduk.itseats.order.dto;

public interface NearbyOrderDto {
    Long getOrderId();
    String getStoreName();
    Double getDistance();
    Integer getDeliveryFee();
    String getDeliveryAddress();
}