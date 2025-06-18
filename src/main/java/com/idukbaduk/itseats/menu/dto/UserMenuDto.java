package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMenuDto {
    private Long menuId;
    private String imageUrl;
    private String name;
    private Long price;
    private String description;
    private Double rating;
}
