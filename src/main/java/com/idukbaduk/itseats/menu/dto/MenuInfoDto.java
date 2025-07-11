package com.idukbaduk.itseats.menu.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MenuInfoDto {
    private Long menuId;
    private String menuName;
    private String menuPrice;
    private String menuStatus;
    private String menuGroupName;
    private int menuPriority;
}
