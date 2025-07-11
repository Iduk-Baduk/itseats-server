package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderReceptionDTO {
    private String menuName;
    private int quantity;
    private int price;
    private String menuOption;
}
