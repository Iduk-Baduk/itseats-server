package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.entity.Store;
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
@Transactional(readOnly = true)
public class MenuGroupService {

    private final MenuRepository menuRepository;
    private final MenuGroupRepository menuGroupRepository;
    private final StoreRepository storeRepository;

    @Transactional
    public MenuGroupResponse saveMenuGroup(Long storeId, MenuGroupRequest request) {
        // 요청에 없는 그룹 : 하위 메뉴 있으면 isActive=false, 없으면 삭제
        // 요청에 있는 그룹 : 기존에 있으면 이름/순서/활성화 여부 업데이트, 없던 그룹이면 새로 생성

        sortMenuGroupRequest(request);

        List<MenuGroup> existingGroups = menuGroupRepository.findMenuGroupsByStoreId(storeId);
        updateMenuGroups(storeId, request, existingGroups);

        return createResponse(storeId);
    }

    private void sortMenuGroupRequest(MenuGroupRequest request) {
        request.getMenuGroups().sort(Comparator.comparing(MenuGroupDto::getMenuGroupPriority));
    }

    private void updateMenuGroups(Long storeId, MenuGroupRequest request, List<MenuGroup> existingGroups) {
        // 요청에 있는 그룹 이름으로 Map 생성
        Map<String, MenuGroupDto> requestedMap = request.getMenuGroups().stream()
                .collect(Collectors.toMap(MenuGroupDto::getMenuGroupName, Function.identity()));

        // 기존 그룹 중 요청에 없는 그룹 처리 : 비활성화 또는 삭제
        processRemovedGroups(existingGroups, requestedMap);
        // 요청에 있는 그룹 처리 : 업데이트 또는 생성
        processRequestedGroups(storeId, request, existingGroups);
    }


    private void processRemovedGroups(List<MenuGroup> existingGroups, Map<String, MenuGroupDto> requestedMap) {
        for (MenuGroup group : existingGroups) {
            String name = group.getMenuGroupName();
            if (!requestedMap.containsKey(name)) {
                if (menuRepository.existsByMenuGroup(group)) {
                    group.setMenuGroupPriority(999);
                    group.setMenuGroupIsActive(false); // 메뉴 있으면 비활성화
                } else {
                    menuGroupRepository.delete(group); // 메뉴 없으면 삭제
                }
            }
        }
    }

    private void processRequestedGroups(Long storeId, MenuGroupRequest request, List<MenuGroup> existingGroups) {
        Store store = storeRepository.findById(storeId).orElseThrow(
                () -> new StoreException(StoreErrorCode.STORE_NOT_FOUND)
        );

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
                menuGroupRepository.save(createMenuGroup(storeId, dto, store));
            }
        }
    }

    private MenuGroup createMenuGroup(Long storeId, MenuGroupDto dto, Store store) {
        return MenuGroup.builder()
                .store(store)
                .menuGroupName(dto.getMenuGroupName())
                .menuGroupPriority(dto.getMenuGroupPriority())
                .menuGroupIsActive(dto.isMenuGroupIsActive())
                .build();
    }

    private MenuGroupResponse createResponse(Long storeId) {
        List<MenuGroupDto> result = menuGroupRepository.findMenuGroupsByStoreId(storeId).stream()
                .map(this::createMenuGroupDto)
                .toList();

        return MenuGroupResponse.builder()
                .menuGroups(result)
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
