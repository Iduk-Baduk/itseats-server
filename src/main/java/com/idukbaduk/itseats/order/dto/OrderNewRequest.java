package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Builder
public class OrderNewRequest {

    @Setter
    private Long addrId;
    private Long storeId;
    private List<OrderMenuDTO> orderMenus;
    private List<Long> coupons;
    private String deliveryType;
}
