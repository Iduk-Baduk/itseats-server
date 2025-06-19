package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.UserMenuOptionResponse;
import com.idukbaduk.itseats.menu.dto.UserOptionGroupDto;
import com.idukbaduk.itseats.menu.dto.UserOptionDto;
import com.idukbaduk.itseats.menu.entity.*;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final MenuRepository menuRepository;
    private final MenuImageRepository menuImageRepository;
    private final MenuOptionGroupRepository menuOptionGroupRepository;
    private final MenuOptionRepository menuOptionRepository;

    @Transactional(readOnly = true)
    public UserMenuOptionResponse getMenuOptions(Long menuId) {
        Menu menu = menuRepository.findByMenuIdAndDeletedFalse(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

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
}
