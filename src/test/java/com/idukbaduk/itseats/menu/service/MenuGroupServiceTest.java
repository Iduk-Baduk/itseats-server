package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.MenuGroupDto;
import com.idukbaduk.itseats.menu.dto.MenuGroupRequest;
import com.idukbaduk.itseats.menu.dto.MenuGroupResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.entity.Store;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @DisplayName("메뉴 그룹을 설정한다")
    void saveMenuGroup_success() {
        // given
        Long storeId = 1L;
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(new Store()));


        List<MenuGroup> existingMenuGroups = List.of(
                createMenuGroup(10L, "음료", 1, Collections.emptyList()),
                createMenuGroup(20L, "베이커리", 2, Collections.emptyList())
        );
        when(menuGroupRepository.findMenuGroupsByStoreId(storeId)).thenReturn(existingMenuGroups);

        MenuGroupRequest request = MenuGroupRequest.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("커피", 1),
                        createMenuGroupDto("음료", 2)
                )))
                .build();

        // when
        MenuGroupResponse result = menuGroupService.saveMenuGroup(storeId, request);

        // then
        assertThat(existingMenuGroups.get(0).getMenuGroupPriority()).isEqualTo(2);
        verify(menuGroupRepository).delete(argThat(group -> group.getMenuGroupName().equals("베이커리")));
        verify(menuGroupRepository).save(argThat(group -> group.getMenuGroupName().equals("커피")));
    }

    private MenuGroup createMenuGroup(long groupId, String groupName, int priority, List<Menu> menus) {
        return MenuGroup.builder()
                .menuGroupId(groupId)
                .menuGroupName(groupName)
                .menuGroupPriority(priority)
                .menuGroupIsActive(true)
                .menus(menus)
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