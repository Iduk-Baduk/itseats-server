package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.error.enums.StoreErrorCode;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final StoreRepository storeRepository;

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

    @Transactional
    public MenuGroupResponse saveMenuGroup(Long storeId, MenuGroupRequest request) {
        // 요청에 없는 그룹 : 하위 메뉴 있으면 isActive=false, 없으면 삭제
        // 요청에 있는 그룹 : 기존에 있으면 이름/순서/활성화 여부 업데이트, 없던 그룹이면 새로 생성
        sortMenuGroupRequest(request);

        List<MenuGroup> existingGroups = menuGroupRepository.findMenuGroupsByStoreId(storeId);

        // 요청에 있는 그룹 이름으로 Map 생성
        Map<String, MenuGroupDto> requestedMap = request.getMenuGroups().stream()
                .collect(Collectors.toMap(MenuGroupDto::getMenuGroupName, Function.identity()));

        // 기존 그룹 중 요청에 없는 그룹 처리
        for (MenuGroup group : existingGroups) {
            String name = group.getMenuGroupName();
            if (!requestedMap.containsKey(name)) {
                boolean hasMenus = menuRepository.existsByMenuGroup(group);
                if (hasMenus) {
                    group.setMenuGroupIsActive(false); // 메뉴 있으면 비활성화
                } else {
                    menuGroupRepository.delete(group); // 메뉴 없으면 삭제
                }
            }
        }

        // 요청에 있는 그룹 처리
        for (MenuGroupDto dto : request.getMenuGroups()) {
            Optional<MenuGroup> existing = existingGroups.stream()
                    .filter(g -> g.getMenuGroupName().equals(dto.getMenuGroupName()))
                    .findFirst();

            if (existing.isPresent()) {
                // 이미 존재하면 수정
                MenuGroup group = existing.get();
                group.setMenuGroupPriority(dto.getMenuGroupPriority());
                group.setMenuGroupIsActive(dto.isMenuGroupIsActive());
            } else {
                // 존재하지 않으면 새로 생성
                menuGroupRepository.save(createMenuGroup(storeId, dto));
            }
        }

        // 응답 반환용 DTO 생성
        List<MenuGroupDto> result = menuGroupRepository.findMenuGroupsByStoreId(storeId).stream()
                .map(this::createMenuGroupDto)
                .toList();

        return MenuGroupResponse.builder()
                .menuGroups(result)
                .build();
    }

    private void sortMenuGroupRequest(MenuGroupRequest request) {
        request.getMenuGroups().sort(Comparator.comparing(MenuGroupDto::getMenuGroupPriority));
    }

    private MenuGroup createMenuGroup(Long storeId, MenuGroupDto dto) {
        return MenuGroup.builder()
                .store(storeRepository.findByStoreId(storeId).orElseThrow(
                        () -> new StoreException(StoreErrorCode.STORE_NOT_FOUND
                        )))
                .menuGroupName(dto.getMenuGroupName())
                .menuGroupPriority(dto.getMenuGroupPriority())
                .menuGroupIsActive(dto.isMenuGroupIsActive())
                .build();
    }

    private MenuGroupDto createMenuGroupDto(MenuGroup menuGroup) {
        return MenuGroupDto.builder()
                .menuGroupName(menuGroup.getMenuGroupName())
                .menuGroupPriority(menuGroup.getMenuGroupPriority())
                .menuGroupIsActive(menuGroup.isMenuGroupIsActive())
                .build();
    }
}
