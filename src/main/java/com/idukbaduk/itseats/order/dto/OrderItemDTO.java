package com.idukbaduk.itseats.order.dto;

import com.idukbaduk.itseats.order.entity.OrderMenu;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OrderItemDTO {

    private String menuName;
    private int quantity;
    private int menuPrice;
    private String options;

    public static OrderItemDTO of(OrderMenu orderMenu) {
        return OrderItemDTO.builder()
                .menuName(orderMenu.getMenuName())
                .quantity(orderMenu.getQuantity())
                .menuPrice(orderMenu.getPrice())
                .options(orderMenu.getMenuOption())
                .build();
    }
}
