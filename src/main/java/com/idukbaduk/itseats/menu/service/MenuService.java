package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.entity.*;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
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

        return toMenuListResponse(menus, totalMenuCount, orderableMenuCount, outOfStockTodayCount, hiddenMenuCount);
    }

    public MenuDetailResponse getMenuDetail(Long storeId, Long menuId) {
        Menu menu = menuRepository.findDetailById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        if (!menu.getMenuGroup().getStore().getStoreId().equals(storeId)) {
            throw new MenuException(MenuErrorCode.MENU_ACCESS_DENIED);
        }

        return MenuDetailResponse.builder()
                .menuId(menu.getMenuId())
                .menuName(menu.getMenuName())
                .menuDescription(menu.getMenuDescription())
                .menuPrice(menu.getMenuPrice())
                .menuStatus(menu.getMenuStatus().name())
                .menuRating(menu.getMenuRating())
                .menuGroupName(menu.getMenuGroup().getMenuGroupName())
                .optionGroups(optionGroupsToDto(menu.getMenuOptionGroups()))
                .build();
    }

    public Menu getMenu(Long menuId) {
        return menuRepository.findById(menuId)
                .orElseThrow(() -> new MenuException(MenuErrorCode.MENU_NOT_FOUND));
    }

    @Transactional
    public MenuResponse createMenu(Long storeId, MenuRequest request, List<MultipartFile> imageFiles) {
        MenuGroup menuGroup = findMenuGroup(storeId, request.getMenuGroupName());
        validateDuplicateOptionGroupNames(request.getOptionGroups());
        validateOptionSelectRange(request.getOptionGroups());

        Menu menu = createBaseMenu(request, menuGroup);
        menu.setMenuOptionGroups(createOptionGroups(menu, request.getOptionGroups())); // 옵션 그룹 생성 및 추가
        Menu savedMenu = menuRepository.save(menu); // cascade에 의해 옵션도 모두 저장됨

        List<MenuImage> images = menuMediaService.createMenuImages(savedMenu, imageFiles);

        return toResponse(savedMenu, images);
    }

    @Transactional
    public MenuResponse updateMenu(Long storeId, Long menuId, MenuRequest request, List<MultipartFile> imageFiles) {
        Menu menu = menuRepository.findByStoreIdAndMenuId(storeId, menuId).orElseThrow(
                () -> new MenuException(MenuErrorCode.MENU_NOT_FOUND)
        );
        MenuGroup menuGroup = findMenuGroup(storeId, request.getMenuGroupName());
        validateDuplicateOptionGroupNames(request.getOptionGroups());
        validateOptionSelectRange(request.getOptionGroups());

        updateMenuFields(request, menu, menuGroup); // menu 필드 업데이트
        menu.setMenuOptionGroups(createOptionGroups(menu, request.getOptionGroups())); // 옵션 그룹 삭제 후 추가
        Menu savedMenu = menuRepository.save(menu); // cascade에 의해 옵션도 모두 저장됨

        List<MenuImage> images = menuMediaService.updateMenuImages(savedMenu, imageFiles);

        return toResponse(savedMenu, images);
    }

    @Transactional
    public void deleteMenu(Long storeId, Long menuId) {
        Menu menu = menuRepository.findByStoreIdAndMenuId(storeId, menuId).orElseThrow(
                () -> new MenuException(MenuErrorCode.MENU_NOT_FOUND)
        );
        menuRepository.delete(menu); // is_deleted = true 설정 (@SQLDelete 이용)
    }

    @Transactional
    public MenuListResponse updateMenuPriority(Long storeId, MenuPriorityRequest request) {
        List<Menu> menus = menuRepository.findByStoreId(storeId);

        Map<Long, Integer> priorityMap = new HashMap<>();
        request.getMenus().forEach(m -> {
            priorityMap.put(m.getMenuId(), m.getMenuPriority());
        });

        for (Menu m : menus) {
            if (priorityMap.containsKey(m.getMenuId()))
                m.updateMenuPriority(priorityMap.get(m.getMenuId()));
        }
        menuRepository.saveAll(menus);
        sortMenusPreservingGroupOrder(menus);

        int totalMenuCount = menus.size();
        int orderableMenuCount = getMenuCount(menus, MenuStatus.ON_SALE);
        int outOfStockTodayCount = getMenuCount(menus, MenuStatus.OUT_OF_STOCK);
        int hiddenMenuCount = getMenuCount(menus, MenuStatus.HIDDEN);

        return toMenuListResponse(menus, totalMenuCount, orderableMenuCount, outOfStockTodayCount, hiddenMenuCount);
    }

    private int getMenuCount(List<Menu> menus, MenuStatus menuStatus) {
        return (int) menus.stream()
                .filter(m -> m.getMenuStatus() == menuStatus)
                .count();
    }

    private MenuListResponse toMenuListResponse(List<Menu> menus, int totalMenuCount, int orderableMenuCount, int outOfStockTodayCount, int hiddenMenuCount) {
        List<MenuInfoDto> menuInfos = menus.stream()
                .map(menu -> MenuInfoDto.builder()
                        .menuId(menu.getMenuId())
                        .menuName(menu.getMenuName())
                        .menuPrice(String.valueOf(menu.getMenuPrice()))
                        .menuStatus(menu.getMenuStatus().name())
                        .menuGroupName(menu.getMenuGroup().getMenuGroupName())
                        .menuPriority(menu.getMenuPriority())
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

    private void updateMenuFields(MenuRequest request, Menu menu, MenuGroup menuGroup) {
        menu.updateMenu(
                menuGroup,
                request.getMenuName(),
                request.getMenuPrice(),
                request.getMenuStatus(),
                request.getMenuDescription(),
                request.getMenuPriority()
        );
    }

    private List<MenuOptionGroup> createOptionGroups(Menu menu, List<MenuOptionGroupDto> optionGroups) {
        List<MenuOptionGroup> optionGroupsCreated = new ArrayList<>();

        for (MenuOptionGroupDto groupDto : optionGroups) {
            MenuOptionGroup optionGroup = MenuOptionGroup.builder()
                    .menu(menu)
                    .optGroupName(groupDto.getOptionGroupName())
                    .isRequired(groupDto.isRequired())
                    .minSelect(groupDto.getMinSelect())
                    .maxSelect(groupDto.getMaxSelect())
                    .optGroupPriority(groupDto.getPriority())
                    .build();
            optionGroupsCreated.add(optionGroup);

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
        return optionGroupsCreated;
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

    private void sortMenusPreservingGroupOrder(List<Menu> menus) {
        // menuGroup의 순서를 유지하면서 menuPriority로 정렬
        Map<Long, Integer> groupOrder = new LinkedHashMap<>();
        int order = 0;
        for (Menu menu : menus) {
            Long groupId = menu.getMenuGroup().getMenuGroupId();
            if (!groupOrder.containsKey(groupId)) {
                groupOrder.put(groupId, order++);
            }
        }

        menus.sort(Comparator
                .comparing((Menu m) -> groupOrder.get(m.getMenuGroup().getMenuGroupId()))
                .thenComparing(Menu::getMenuPriority));
    }
}
