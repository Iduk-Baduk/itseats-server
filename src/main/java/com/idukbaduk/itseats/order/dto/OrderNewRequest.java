package com.idukbaduk.itseats.order.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class OrderNewRequest {

    private Long addrId;
    private Long storeId;
    private List<OrderMenuDTO> orderMenus;
    private List<Long> coupons;
    private String deliveryType;
}
