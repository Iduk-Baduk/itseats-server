package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.MenuInfoDto;
import com.idukbaduk.itseats.menu.dto.MenuListRequest;
import com.idukbaduk.itseats.menu.dto.MenuListResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;

    public MenuListResponse getMenuList(Long storeId, MenuListRequest request) {

        List<Menu> menus = menuRepository.findMenusByStore(
                storeId,
                request.getMenuGroup(),
                request.getKeyword()
        );

        if (menus.isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_NOT_FOUND);
        }

        int totalMenuCount = menus.size();
        int orderableMenuCount = getMenuCount(menus, MenuStatus.ON_SALE);
        int outOfStockTodayCount = getMenuCount(menus, MenuStatus.OUT_OF_STOCK);
        int hiddenMenuCount = getMenuCount(menus, MenuStatus.HIDDEN);

        List<MenuInfoDto> menuInfos = menus.stream()
                .map(menu -> MenuInfoDto.builder()
                        .menuId(menu.getMenuId())
                        .menuName(menu.getMenuName())
                        .menuPrice(String.valueOf(menu.getMenuPrice()))
                        .menuStatus(menu.getMenuStatus().name())
                        .menuGroupName(menu.getMenuGroup().getMenuGroupName())
                        .build())
                .toList();

        return MenuListResponse.builder()
                .totalMenuCount(totalMenuCount)
                .orderableMenuCount(orderableMenuCount)
                .outOfStockTodayCount(outOfStockTodayCount)
                .hiddenMenuCount(hiddenMenuCount)
                .menus(menuInfos)
                .build();
    }

    private int getMenuCount(List<Menu> menus, MenuStatus menuStatus) {
        return (int) menus.stream()
                .filter(m -> m.getMenuStatus() == menuStatus)
                .count();
    }

    public Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));
    }
}
