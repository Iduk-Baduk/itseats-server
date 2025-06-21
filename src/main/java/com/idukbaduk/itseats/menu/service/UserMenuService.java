package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.UserMenuOptionResponse;
import com.idukbaduk.itseats.menu.dto.UserOptionGroupDto;
import com.idukbaduk.itseats.menu.dto.UserOptionDto;
import com.idukbaduk.itseats.menu.entity.*;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.*;
import com.idukbaduk.itseats.menu.dto.UserMenuDto;
import com.idukbaduk.itseats.menu.dto.UserMenuGroupDto;
import com.idukbaduk.itseats.menu.dto.UserMenuListResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final MenuRepository menuRepository;
    private final MenuImageRepository menuImageRepository;
    private final MenuOptionGroupRepository menuOptionGroupRepository;
    private final MenuGroupRepository menuGroupRepository;

    @Transactional(readOnly = true)
    public UserMenuOptionResponse getMenuOptions(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByMenuIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        if (!menu.getMenuGroup().getStore().getStoreId().equals(storeId)) {
            throw new MenuException(MenuErrorCode.MENU_NOT_BELONG_TO_STORE);
        }

        String imageUrl = menuImageRepository.findFirstByMenu_MenuIdOrderByDisplayOrderAsc(menuId)
                .map(MenuImage::getImageUrl)
                .orElse(null);

        List<MenuOptionGroup> optionGroups = menuOptionGroupRepository
                .findGroupsWithOptionsByMenuId(menuId);

        List<UserOptionGroupDto> optionGroupDtos = optionGroups.stream()
                .map(group -> UserOptionGroupDto.builder()
                        .optionGroupName(group.getOptGroupName())
                        .isRequired(group.isRequired())
                        .minSelect(group.getMinSelect())
                        .maxSelect(group.getMaxSelect())
                        .priority(group.getOptGroupPriority())
                        .options(group.getOptions().stream()
                                .map(option -> UserOptionDto.builder()
                                        .optionId(option.getOptionId())
                                        .optionName(option.getOptionName())
                                        .optionPrice(option.getOptionPrice())
                                        .optionStatus(option.getOptionStatus().name())
                                        .optionPriority(option.getOptionPriority())
                                        .isSelected(false)
                                        .build())
                                .toList())
                        .build())
                .toList();

        return UserMenuOptionResponse.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuDescription(menu.getMenuDescription())
                .menuPrice(menu.getMenuPrice())
                .menuStatus(menu.getMenuStatus().name())
                .menuGroupName(menu.getMenuGroup().getMenuGroupName())
                .imageUrl(imageUrl)
                .optionGroups(optionGroupDtos)
                .build();
    }

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
