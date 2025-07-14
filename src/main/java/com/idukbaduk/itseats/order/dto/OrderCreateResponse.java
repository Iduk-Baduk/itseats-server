package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderCreateResponse {

    private Long orderId;
    private String tossOrderId;
}
