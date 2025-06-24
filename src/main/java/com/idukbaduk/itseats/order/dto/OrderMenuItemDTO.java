package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderMenuItemDTO {
    private Long menuId;
    private String menuName;
    private int quantity;
    private int menuPrice;
    private List<String> options;
}
