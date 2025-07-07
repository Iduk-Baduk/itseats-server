package com.idukbaduk.itseats.rider.dto;

public record ReadyOrderResponse(
        String deliveryType,
        String storeName,
        double deliveryDistance,
        int deliveryFee,
        String deliveryAddress
) {
}
