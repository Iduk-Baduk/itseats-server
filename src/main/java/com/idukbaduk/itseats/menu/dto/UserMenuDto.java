package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMenuDto {
    private Long menuId;
    private String imageUrl;
    private String name;
    private int price;
    private String description;
    private double rating;
}
