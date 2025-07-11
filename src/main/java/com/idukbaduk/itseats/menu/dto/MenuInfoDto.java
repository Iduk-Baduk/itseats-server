package com.idukbaduk.itseats.menu.dto;

import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

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
    private List<String> images;

    public static MenuInfoDto of(Menu menu, List<MenuImage> images) {
        return MenuInfoDto.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuPrice(String.valueOf(menu.getMenuPrice()))
                .menuStatus(menu.getMenuStatus().name())
                .menuGroupName(menu.getMenuGroup().getMenuGroupName())
                .menuPriority(menu.getMenuPriority())
                .images(images.stream().map(MenuImage::getImageUrl).toList())
                .build();
    }
}
