package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserMenuOptionResponse {
    private Long menuId;
    private String menuName;
    private String menuDescription;
    private Long menuPrice;
    private String menuStatus;
    private String menuGroupName;
    private String imageUrl;
    private List<UserOptionGroupDto> optionGroups;
}
