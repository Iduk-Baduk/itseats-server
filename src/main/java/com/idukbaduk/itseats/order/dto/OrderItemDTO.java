package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemDTO {

    private String menuName;
    private int quantity;
    private int menuPrice;
    private String options;
}
