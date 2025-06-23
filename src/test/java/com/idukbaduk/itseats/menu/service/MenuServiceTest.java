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
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuGroupRepository menuGroupRepository;
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("메뉴가 없으면 예외 발생")
    void getMenuList_noMenus_throwsException() {
        // given
        Long storeId = 1L;
        MenuListRequest request = MenuListRequest.builder()
                // storeId 필드 제거
                .build();

        when(menuRepository.findMenusByStore(eq(storeId), any(), any()))
                .thenReturn(Collections.emptyList());

        // when & then
        assertThatThrownBy(() -> menuService.getMenuList(storeId, request))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("정상적으로 메뉴 목록을 반환한다")
    void getMenuList_success() {
        // given
        Long storeId = 1L;
        MenuGroup menuGroup = MenuGroup.builder()
                .menuGroupId(10L)
                .menuGroupName("음료")
                .build();

        Menu menu1 = Menu.builder()
                .menuId(11L)
                .menuName("아메리카노")
                .menuPrice(2000L)
                .menuStatus(MenuStatus.ON_SALE)
                .menuGroup(menuGroup)
                .build();

        Menu menu2 = Menu.builder()
                .menuId(12L)
                .menuName("초코라떼")
                .menuPrice(2500L)
                .menuStatus(MenuStatus.OUT_OF_STOCK)
                .menuGroup(menuGroup)
                .build();

        List<Menu> mockMenus = Arrays.asList(menu1, menu2);

        MenuListRequest request = MenuListRequest.builder()
                // storeId 필드 제거
                .menuGroup("음료")
                .keyword("라떼")
                .build();

        when(menuRepository.findMenusByStore(storeId, "음료", "라떼"))
                .thenReturn(mockMenus);

        // when
        MenuListResponse response = menuService.getMenuList(storeId, request);

        // then
        assertThat(response.getTotalMenuCount()).isEqualTo(2);
        assertThat(response.getOrderableMenuCount()).isEqualTo(1);
        assertThat(response.getOutOfStockTodayCount()).isEqualTo(1);
        assertThat(response.getMenus()).extracting(MenuInfoDto::getMenuName)
                .containsExactly("아메리카노", "초코라떼");
    }

    @Test
    @DisplayName("메뉴 정보를 성공적으로 반환")
    void getMenu_success() {
        // given
        Long menuId = 1L;
        Menu menu = Menu.builder()
                .menuId(menuId)
                .build();

        when(menuRepository.findById(menuId)).thenReturn(Optional.of(menu));

        // when
        Menu result = menuService.getMenu(menuId);

        // then
        assertThat(result.getMenuId()).isEqualTo(menu.getMenuId());
    }

    @Test
    @DisplayName("존재하지 않는 메뉴 조회시 예외 발생")
    void getMenu_notExist() {
        // given
        Long menuId = 1L;
        when(menuRepository.findById(menuId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> menuService.getMenu(menuId))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_NOT_FOUND.getMessage());
    }

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
        MenuGroupResponse result = menuService.saveMenuGroup(storeId, request);

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