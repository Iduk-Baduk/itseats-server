package com.idukbaduk.itseats.order.dto;

public interface NearbyOrderDTO {
    Long getOrderId();
    String getStoreName();
    Double getDistance();
    Integer getDeliveryFee();
    String getDeliveryAddress();
}