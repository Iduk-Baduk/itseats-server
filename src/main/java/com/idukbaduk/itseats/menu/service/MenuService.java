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

    public MenuListResponse getMenuList(MenuListRequest request) {
        if (request.getStoreId() == null) {
            throw new MenuException(MenuErrorCode.STORE_ID_REQUIRED);
        }

        List<Menu> menus = menuRepository.findMenusByStore(
                request.getStoreId(),
                request.getMenuGroup(),
                request.getKeyword()
        );

        if (menus.isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_NOT_FOUND);
        }

        int totalMenuCount = menus.size();
        int orderableMenuCount = (int) menus.stream()
                .filter(m -> m.getMenuStatus() == MenuStatus.ON_SALE)
                .count();
        int outOfStockTodayCount = (int) menus.stream()
                .filter(m -> m.getMenuStatus() == MenuStatus.OUT_OF_STOCK)
                .count();
        int hiddenMenuCount = (int) menus.stream()
                .filter(m -> m.getMenuStatus() == MenuStatus.HIDDEN)
                .count();

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
}