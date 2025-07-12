package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderCreateRequest {

    private Long addrId;
    private Long storeId;
    private List<OrderMenuDTO> orderMenus;
    private String deliveryType;
}
