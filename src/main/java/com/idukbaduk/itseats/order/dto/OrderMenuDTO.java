package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class OrderMenuDTO {

    private Long menuId;
    private String menuName;
    private List<MenuOptionDTO> menuOption;
    private int menuTotalPrice;
    private int quantity;
}
