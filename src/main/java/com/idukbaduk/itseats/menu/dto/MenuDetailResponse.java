package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuDetailResponse {
    private Long menuId;
    private String menuName;
    private String menuDescription;
    private Long menuPrice;
    private String menuStatus;
    private Float menuRating;
    private String menuGroupName;
    private Integer menuPriority;
    private List<MenuOptionGroupDto> optionGroups;
}
