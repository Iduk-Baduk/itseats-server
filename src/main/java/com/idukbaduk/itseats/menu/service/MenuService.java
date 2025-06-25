package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.entity.*;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuOptionGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuOptionRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final MenuMediaService menuMediaService;

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

    public MenuResponse createMenu(Long storeId, MenuRequest request) {
        MenuGroup menuGroup = findMenuGroup(storeId, request.getMenuGroupName());
        validateDuplicateOptionGroupNames(request.getOptionGroups());
        validateOptionSelectRange(request.getOptionGroups());

        Menu menu = createBaseMenu(request, menuGroup);
        createAndAttachOptions(menu, request.getOptionGroups());
        Menu savedMenu = menuRepository.save(menu); // cascade에 의해 옵션도 모두 저장됨

        List<MenuImage> images = menuMediaService.createMenuImages(savedMenu, request.getImages());

        return toResponse(savedMenu, images);
    }

    private MenuGroup findMenuGroup(Long storeId, String menuGroupName) {
        return menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(menuGroupName, storeId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_GROUP_NOT_FOUND));
    }

    private void validateDuplicateOptionGroupNames(List<MenuOptionGroupDto> optionGroups) {
        // 한 메뉴에 대해서 동일한 optGroupName이 존재하면 예외 처리
        Set<String> nameSet = new HashSet<>();
        for (MenuOptionGroupDto groupDto : optionGroups) {
            if (!nameSet.add(groupDto.getOptionGroupName())) {
                throw new MenuException(MenuErrorCode.OPTION_GROUP_NAME_DUPLICATED);
            }
        }
    }

    private void validateOptionSelectRange(List<MenuOptionGroupDto> optionGroups) {
        // 옵션 그룹의 maxSelect가 minSelect보다 작으면 예외 처리
        for (MenuOptionGroupDto groupDto : optionGroups) {
            if (groupDto.getMaxSelect() < groupDto.getMinSelect()) {
                throw new MenuException(MenuErrorCode.OPTION_GROUP_RANGE_INVALID);
            }
        }
    }

    private Menu createBaseMenu(MenuRequest request, MenuGroup menuGroup) {
        return Menu.builder()
                .menuGroup(menuGroup)
                .menuName(request.getMenuName())
                .menuPrice(request.getMenuPrice())
                .menuStatus(request.getMenuStatus())
                .menuRating(0f)
                .menuDescription(request.getMenuDescription())
                .menuPriority(request.getMenuPriority())
                .build();
    }

    private void createAndAttachOptions(Menu menu, List<MenuOptionGroupDto> optionGroups) {
        for (MenuOptionGroupDto groupDto : optionGroups) {
            MenuOptionGroup optionGroup = MenuOptionGroup.builder()
                    .menu(menu)
                    .optGroupName(groupDto.getOptionGroupName())
                    .isRequired(groupDto.isRequired())
                    .minSelect(groupDto.getMinSelect())
                    .maxSelect(groupDto.getMaxSelect())
                    .optGroupPriority(groupDto.getPriority())
                    .build();
            menu.addMenuOptionGroup(optionGroup);

            for (MenuOptionDto optionDto : groupDto.getOptions()) {
                MenuOption option = MenuOption.builder()
                        .menuOptionGroup(optionGroup)
                        .optionName(optionDto.getOptionName())
                        .optionPrice(optionDto.getOptionPrice())
                        .optionStatus(optionDto.getOptionStatus())
                        .optionPriority(optionDto.getOptionPriority())
                        .build();
                optionGroup.addOption(option);
            }
        }
    }

    private MenuResponse toResponse(Menu menu, List<MenuImage> images) {
        return MenuResponse.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuDescription(menu.getMenuDescription())
                .menuPrice(menu.getMenuPrice())
                .menuStatus(menu.getMenuStatus())
                .menuGroupName(menu.getMenuGroup().getMenuGroupName())
                .menuPriority(menu.getMenuPriority())
                .images(images.stream().map(MenuImage::getImageUrl).toList())
                .optionGroups(optionGroupsToDto(menu.getMenuOptionGroups()))
                .build();
    }

    private List<MenuOptionGroupDto> optionGroupsToDto(List<MenuOptionGroup> optionGroups) {
        return optionGroups.stream()
                .map(og -> MenuOptionGroupDto.builder()
                        .optionGroupName(og.getOptGroupName())
                        .isRequired(og.isRequired())
                        .minSelect(og.getMinSelect())
                        .maxSelect(og.getMaxSelect())
                        .priority(og.getOptGroupPriority())
                        .options(optionToDto(og.getOptions()))
                        .build()
                )
                .toList();
    }

    private List<MenuOptionDto> optionToDto(List<MenuOption> options) {
        return options.stream()
                .map(op ->MenuOptionDto.builder()
                        .optionName(op.getOptionName())
                        .optionPrice(op.getOptionPrice())
                        .optionStatus(op.getOptionStatus())
                        .optionPriority(op.getOptionPriority())
                        .build())
                .toList();
    }
}
