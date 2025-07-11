package com.idukbaduk.itseats.menu.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MenuListResponse {

    private int totalMenuCount;
    private int orderableMenuCount;
    private int outOfStockTodayCount;
    private int hiddenMenuCount;
    private List<MenuInfoDto> menus;
}