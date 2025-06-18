package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.UserMenuDto;
import com.idukbaduk.itseats.menu.dto.UserMenuGroupDto;
import com.idukbaduk.itseats.menu.dto.UserMenuListResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private MenuGroupRepository menuGroupRepository;
    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuImageRepository menuImageRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("가게별 메뉴 그룹 및 메뉴 리스트 반환")
    @Test
    void getMenusByStore_success() {
        // given
        Long storeId = 1L;
        MenuGroup group = MenuGroup.builder()
                .menuGroupId(10L)
                .menuGroupName("추천 메뉴")
                .menuGroupPriority(1)
                .menuGroupIsActive(true)
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

        MenuImage image = MenuImage.builder()
                .imageId(1000L)
                .imageUrl("test_url")
                .displayOrder(1)
                .menu(menu)
                .build();

        when(menuGroupRepository.findByStore_storeIdAndMenuGroupIsActiveTrueOrderByMenuGroupPriority(storeId))
                .thenReturn(List.of(group));
        when(menuRepository.findByMenuGroup_MenuGroupIdAndDeletedFalseOrderByMenuPriority(group.getMenuGroupId()))
                .thenReturn(List.of(menu));
        when(menuImageRepository.findFirstByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId()))
                .thenReturn(Optional.of(image));

        // when
        UserMenuListResponse response = userService.getMenusByStore(storeId);

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

        // verify
        verify(menuGroupRepository).findByStore_storeIdAndMenuGroupIsActiveTrueOrderByMenuGroupPriority(storeId);
        verify(menuRepository).findByMenuGroup_MenuGroupIdAndDeletedFalseOrderByMenuPriority(group.getMenuGroupId());
        verify(menuImageRepository).findFirstByMenu_MenuIdOrderByDisplayOrderAsc(menu.getMenuId());
    }
}
