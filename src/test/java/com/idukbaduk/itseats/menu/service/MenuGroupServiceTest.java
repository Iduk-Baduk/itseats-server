package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.MenuGroupDto;
import com.idukbaduk.itseats.menu.dto.MenuGroupRequest;
import com.idukbaduk.itseats.menu.dto.MenuGroupResponse;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.error.StoreException;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuGroupServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuGroupRepository menuGroupRepository;
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MenuGroupService menuGroupService;

    @Test
    @DisplayName("메뉴 그룹을 조회한다")
    void getMenuGroup_success() {
        // given
        Long storeId = 1L;
        List<MenuGroup> menuGroups = List.of(
                createMenuGroup(10L, "음료", 1, true),
                createMenuGroup(20L, "베이커리", 999, false)
        );
        when(menuGroupRepository.findMenuGroupsByStoreId(storeId)).thenReturn(menuGroups);

        // when
        MenuGroupResponse data = menuGroupService.getMenuGroup(storeId);

        // then
        assertThat(data.getMenuGroups()).hasSize(2)
                .extracting("menuGroupName", "menuGroupIsActive", "displayName")
                .containsExactlyInAnyOrder(
                        tuple("음료", true, "음료"),
                        tuple("베이커리", false, "베이커리 (비활성화)")
                );
    }

    @Test
    @DisplayName("메뉴 그룹을 설정한다")
    void saveMenuGroup_success() {
        // given
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(new Store()));


        List<MenuGroup> existingMenuGroups = List.of(
                createMenuGroup(10L, "음료", 1, true),
                createMenuGroup(20L, "베이커리", 2, true)
        );
        when(menuGroupRepository.findMenuGroupsByStoreId(storeId)).thenReturn(existingMenuGroups);

        MenuGroupRequest request = MenuGroupRequest.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("커피", 1),
                        createMenuGroupDto("음료", 2)
                )))
                .build();

        // when
        menuGroupService.saveMenuGroup(storeId, request);

        // then
        assertThat(existingMenuGroups.get(0).getMenuGroupPriority()).isEqualTo(2);
        verify(menuGroupRepository).delete(argThat(group -> group.getMenuGroupName().equals("베이커리")));
        verify(menuGroupRepository).save(argThat(group -> group.getMenuGroupName().equals("커피")));
    }

    @Test
    @DisplayName("메뉴가 있는 그룹은 삭제되지 않고 비활성화된다")
    void saveMenuGroup_deactivatesGroupWithMenus() {
        // given
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(new Store()));

        List<MenuGroup> existingMenuGroups = List.of(
                createMenuGroup(10L, "음료", 1, true),
                createMenuGroup(20L, "베이커리", 2, true)
        );
        when(menuGroupRepository.findMenuGroupsByStoreId(storeId)).thenReturn(existingMenuGroups);

        // 베이커리 그룹에는 메뉴가 있다고 설정
        when(menuRepository.existsByMenuGroup(any())).thenReturn(false);
        when(menuRepository.existsByMenuGroup(argThat(group ->
                group.getMenuGroupName().equals("베이커리")))).thenReturn(true);

        MenuGroupRequest request = MenuGroupRequest.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("음료", 1),
                        createMenuGroupDto("커피", 2)
                )))
                .build();

        // when
        menuGroupService.saveMenuGroup(storeId, request);

        // then
        // 베이커리 그룹은 비활성화되어야 함 (삭제되지 않음)
        MenuGroup bakeryGroup = existingMenuGroups.stream()
                .filter(group -> group.getMenuGroupName().equals("베이커리"))
                .findFirst().orElse(null);

        assertThat(bakeryGroup).isNotNull();
        assertThat(bakeryGroup.getMenuGroupPriority()).isEqualTo(999);
        assertThat(bakeryGroup.isMenuGroupIsActive()).isFalse();

        verify(menuGroupRepository, never()).delete(argThat(group ->
                group.getMenuGroupName().equals("베이커리")));
        verify(menuGroupRepository).save(argThat(group ->
                group.getMenuGroupName().equals("커피")));
    }

    @Test
    @DisplayName("메뉴 그룹 설정 중 가맹점을 찾을 수 없으면 예외 반환한다")
    void saveMenuGroup_storeNotFound() {
        // given
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.empty());

        List<MenuGroup> existingMenuGroups = Collections.emptyList();
        when(menuGroupRepository.findMenuGroupsByStoreId(storeId)).thenReturn(existingMenuGroups);

        MenuGroupRequest request = MenuGroupRequest.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("커피", 1)
                )))
                .build();

        // when & then
        assertThatThrownBy(() -> menuGroupService.saveMenuGroup(storeId, request))
                .isInstanceOf(StoreException.class);
    }

    private MenuGroup createMenuGroup(long groupId, String groupName, int priority, boolean isActive) {
        return MenuGroup.builder()
                .menuGroupId(groupId)
                .menuGroupName(groupName)
                .menuGroupPriority(priority)
                .menuGroupIsActive(isActive)
                .build();
    }

    private MenuGroupDto createMenuGroupDto(String groupName, int priority) {
        return MenuGroupDto.builder()
                .menuGroupName(groupName)
                .menuGroupPriority(priority)
                .menuGroupIsActive(true)
                .build();
    }
}
