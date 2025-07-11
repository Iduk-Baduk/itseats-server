package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CookTimeResponse {
    private Long orderId;
    private String deliveryEta;
}
