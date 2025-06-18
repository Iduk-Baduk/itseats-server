package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.UserMenuDto;
import com.idukbaduk.itseats.menu.dto.UserMenuGroupDto;
import com.idukbaduk.itseats.menu.dto.UserMenuListResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.*;

class UserMenuServiceTest {

    @Mock
    private MenuGroupRepository menuGroupRepository;
    @Mock
    private MenuImageRepository menuImageRepository;

    @InjectMocks
    private UserMenuService userMenuService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("가게별 메뉴 그룹 및 메뉴 리스트 반환 - 성공 케이스")
    @Test
    void getMenusByStore_success() {
        // given
        Long storeId = 1L;

        MenuGroup group = MenuGroup.builder()
                .menuGroupId(10L)
                .menuGroupName("추천 메뉴")
                .menuGroupPriority(1)
                .menuGroupIsActive(true)
                .menus(Collections.emptyList())
                .build();

        Menu menu = Menu.builder()
                .menuId(100L)
                .menuName("라면")
                .menuPrice(3000)
                .menuDescription("맛있는 라면")
                .menuPriority(1)
                .menuRating(4.8f)
                .menuGroup(group)
                .build();

        group = MenuGroup.builder()
                .menuGroupId(group.getMenuGroupId())
                .menuGroupName(group.getMenuGroupName())
                .menuGroupPriority(group.getMenuGroupPriority())
                .menuGroupIsActive(group.isMenuGroupIsActive())
                .menus(List.of(menu))
                .build();

        MenuImage image = MenuImage.builder()
                .imageId(1000L)
                .imageUrl("test_url")
                .displayOrder(1)
                .menu(menu)
                .build();

        when(menuGroupRepository.findGroupsWithMenusByStoreId(storeId))
                .thenReturn(List.of(group));

        when(menuImageRepository.findByMenu_MenuIdInOrderByMenu_MenuIdAscDisplayOrderAsc(List.of(100L)))
                .thenReturn(List.of(image));

        // when
        UserMenuListResponse response = userMenuService.getMenusByStore(storeId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMenuGroups()).hasSize(1);

        UserMenuGroupDto groupDto = response.getMenuGroups().get(0);
        assertThat(groupDto.getGroupName()).isEqualTo("추천 메뉴");
        assertThat(groupDto.getMenus()).hasSize(1);

        UserMenuDto menuDto = groupDto.getMenus().get(0);
        assertThat(menuDto.getMenuId()).isEqualTo(100L);
        assertThat(menuDto.getImageUrl()).isEqualTo("test_url");
        assertThat(menuDto.getName()).isEqualTo("라면");
        assertThat(menuDto.getPrice()).isEqualTo(3000);
        assertThat(menuDto.getDescription()).isEqualTo("맛있는 라면");
        assertThat(menuDto.getRating()).isCloseTo(4.8d, within(0.0001));
    }
}
