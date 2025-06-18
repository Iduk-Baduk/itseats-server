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
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserMenuService {

    private final MenuGroupRepository menuGroupRepository;
    private final MenuRepository menuRepository;
    private final MenuImageRepository menuImageRepository;

    @Transactional(readOnly = true)
    public UserMenuListResponse getMenusByStore(Long storeId) {
        List<MenuGroup> groups = menuGroupRepository.
                findByStore_storeIdAndMenuGroupIsActiveTrueOrderByMenuGroupPriority(storeId);

        if (groups.isEmpty()) {
            throw new MenuException(MenuErrorCode.MENU_GROUP_NOT_FOUND);
        }

        List<UserMenuGroupDto> groupDtos = groups.stream()
                .map(group -> {
                    List<Menu> menus = menuRepository.
                            findByMenuGroup_MenuGroupIdAndDeletedFalseOrderByMenuPriority(group.getMenuGroupId());

                    List<UserMenuDto> menuDtos = menus.stream()
                            .map(menu -> {
                                String imageUrl = menuImageRepository
                                        .findFirstByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId())
                                        .map(MenuImage::getImageUrl)
                                        .orElse(null);

                                return UserMenuDto.builder()
                                        .menuId(menu.getMenuId())
                                        .imageUrl(imageUrl)
                                        .name(menu.getMenuName())
                                        .price(menu.getMenuPrice())
                                        .description(menu.getMenuDescription())
                                        .rating(menu.getMenuRating() != null ? menu.getMenuRating() : 0.0)
                                        .build();
                            }).toList();

                    return UserMenuGroupDto.builder()
                            .groupName(group.getMenuGroupName())
                            .menus(menuDtos)
                            .build();
                }).toList();

        return UserMenuListResponse.builder()
                .menuGroups(groupDtos)
                .build();
    }
}
