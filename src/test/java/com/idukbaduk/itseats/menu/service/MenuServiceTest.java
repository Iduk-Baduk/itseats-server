package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.MenuInfoDto;
import com.idukbaduk.itseats.menu.dto.MenuListRequest;
import com.idukbaduk.itseats.menu.dto.MenuListResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;

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
}

