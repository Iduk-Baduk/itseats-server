package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.UserMenuDto;
import com.idukbaduk.itseats.menu.dto.UserMenuGroupDto;
import com.idukbaduk.itseats.menu.dto.UserMenuListResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final MenuGroupRepository menuGroupRepository;
    private final MenuImageRepository menuImageRepository;

    @Transactional(readOnly = true)
    public UserMenuListResponse getMenusByStore(Long storeId) {

        List<MenuGroup> groups = menuGroupRepository.findGroupsWithMenusByStoreId(storeId);

        if (groups.isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_GROUP_NOT_FOUND);
        }

        List<Long> menuIds = groups.stream()
                .flatMap(group -> group.getMenus().stream())
                .filter(Objects::nonNull)
                .map(Menu::getMenuId)
                .toList();
        if (menuIds.isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_NOT_FOUND);
        }

        List<MenuImage> images = menuImageRepository
                .findByMenu_MenuIdInOrderByMenu_MenuIdAscDisplayOrderAsc(menuIds);

        Map<Long, String> menuIdToImageUrl = new HashMap<>();
        for (MenuImage image : images) {
            Long menuId = image.getMenu().getMenuId();
            if (!menuIdToImageUrl.containsKey(menuId)) {
                menuIdToImageUrl.put(menuId, image.getImageUrl());
            }
        }

        List<UserMenuGroupDto> groupDtos = groups.stream()
                .map(group -> {
                    List<UserMenuDto> menuDtos = (group.getMenus() == null ? Collections.<Menu>emptyList() : group.getMenus()).stream()
                            .filter(menu -> !menu.isDeleted())
                            .sorted(Comparator.comparingInt(Menu::getMenuPriority))
                            .map(menu -> UserMenuDto.builder()
                                    .menuId(menu.getMenuId())
                                    .imageUrl(menuIdToImageUrl.get(menu.getMenuId()))
                                    .name(menu.getMenuName())
                                    .price(menu.getMenuPrice())
                                    .description(menu.getMenuDescription())
                                    .rating(menu.getMenuRating() != null ? menu.getMenuRating() : 0.0)
                                    .build())
                            .toList();

                    return UserMenuGroupDto.builder()
                            .groupName(group.getMenuGroupName())
                            .menus(menuDtos)
                            .build();
                })
                .toList();

        return UserMenuListResponse.builder()
                .menuGroups(groupDtos)
                .build();
    }
}
