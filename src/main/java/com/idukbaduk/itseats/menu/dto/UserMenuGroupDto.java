package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class UserMenuGroupDto {
    private String groupName;
    private List<UserMenuDto> menus;
}
