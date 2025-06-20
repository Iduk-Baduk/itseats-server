package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.UserMenuOptionResponse;
import com.idukbaduk.itseats.menu.dto.UserOptionGroupDto;
import com.idukbaduk.itseats.menu.dto.UserOptionDto;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.entity.MenuOption;
import com.idukbaduk.itseats.menu.entity.MenuOptionGroup;
import com.idukbaduk.itseats.menu.entity.MenuImage;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import com.idukbaduk.itseats.menu.repository.MenuOptionGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.entity.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

class UserMenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MenuImageRepository menuImageRepository;
    @Mock
    private MenuOptionGroupRepository menuOptionGroupRepository;

    @InjectMocks
    private UserMenuService userMenuService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @DisplayName("존재하지 않는 메뉴 조회 시 예외 발생")
    @Test
    void getMenuOptions_menuNotFound_throwsException() {
        // given
        Long storeId = 1L;
        Long menuId = 999L;
        when(menuRepository.findByMenuIdAndDeletedFalse(menuId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userMenuService.getMenuOptions(storeId, menuId))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_NOT_FOUND.getMessage());
    }

    @DisplayName("메뉴가 가맹점에 속해있지 않은 경우 예외 발생")
    @Test
    void getMenuOptions_menuNotBelongToStore_throwsException() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;

        Store store = Store.builder().storeId(storeId).build();

        Store otherStore = Store.builder().storeId(2L).build();

        MenuGroup menuGroup = MenuGroup.builder()
                .menuGroupId(10L)
                .menuGroupName("음료")
                .store(otherStore)
                .build();

        Menu menu = Menu.builder()
                .menuId(menuId)
                .menuName("아메리카노")
                .menuDescription("평범한 아메리카노입니다.")
                .menuPrice(2000L)
                .menuStatus(MenuStatus.ON_SALE)
                .menuGroup(menuGroup)
                .build();

        when(menuRepository.findByMenuIdAndDeletedFalse(menuId)).thenReturn(Optional.of(menu));

        // when & then
        assertThatThrownBy(() -> userMenuService.getMenuOptions(storeId, menuId))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_NOT_BELONG_TO_STORE.getMessage());
    }

    @DisplayName("메뉴 옵션 조회 성공")
    @Test
    void getMenuOptions_success() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;

        Store store = Store.builder().storeId(storeId).build();

        MenuGroup menuGroup = MenuGroup.builder()
                .menuGroupId(10L)
                .menuGroupName("음료")
                .store(store)
                .build();

        Menu menu = Menu.builder()
                .menuId(menuId)
                .menuName("아메리카노")
                .menuDescription("평범한 아메리카노입니다.")
                .menuPrice(2000L)
                .menuStatus(MenuStatus.ON_SALE)
                .menuGroup(menuGroup)
                .build();

        MenuImage menuImage = MenuImage.builder()
                .imageId(10L)
                .imageUrl("s3_url")
                .menu(menu)
                .displayOrder(1)
                .build();

        MenuOptionGroup optionGroup = MenuOptionGroup.builder()
                .optGroupId(100L)
                .optGroupName("사이즈")
                .isRequired(true)
                .minSelect(1)
                .maxSelect(1)
                .optGroupPriority(1)
                .menu(menu)
                .build();

        MenuOption option1 = MenuOption.builder()
                .optionId(1000L)
                .optionName("톨(Tall)")
                .optionPrice(0L)
                .optionStatus(MenuStatus.ON_SALE)
                .optionPriority(1)
                .menuOptionGroup(optionGroup)
                .build();

        MenuOption option2 = MenuOption.builder()
                .optionId(1001L)
                .optionName("벤티(Venti)")
                .optionPrice(1000L)
                .optionStatus(MenuStatus.HIDDEN)
                .optionPriority(2)
                .menuOptionGroup(optionGroup)
                .build();

        optionGroup = MenuOptionGroup.builder()
                .optGroupId(optionGroup.getOptGroupId())
                .optGroupName(optionGroup.getOptGroupName())
                .isRequired(optionGroup.isRequired())
                .minSelect(optionGroup.getMinSelect())
                .maxSelect(optionGroup.getMaxSelect())
                .optGroupPriority(optionGroup.getOptGroupPriority())
                .menu(menu)
                .options(List.of(option1, option2))
                .build();

        when(menuRepository.findByMenuIdAndDeletedFalse(menuId)).thenReturn(Optional.of(menu));
        when(menuImageRepository.findFirstByMenu_MenuIdOrderByDisplayOrderAsc(menuId)).thenReturn(Optional.of(menuImage));
        when(menuOptionGroupRepository.findGroupsWithOptionsByMenuId(menuId)).thenReturn(List.of(optionGroup));

        // when
        UserMenuOptionResponse response = userMenuService.getMenuOptions(storeId, menuId);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getMenuId()).isEqualTo(menuId);
        assertThat(response.getMenuName()).isEqualTo("아메리카노");
        assertThat(response.getImageUrl()).isEqualTo("s3_url");
        assertThat(response.getMenuGroupName()).isEqualTo("음료");
        assertThat(response.getOptionGroups()).hasSize(1);

        UserOptionGroupDto groupDto = response.getOptionGroups().get(0);
        assertThat(groupDto.getOptionGroupName()).isEqualTo("사이즈");
        assertThat(groupDto.isRequired()).isTrue();
        assertThat(groupDto.getOptions()).hasSize(2);

        UserOptionDto optionDto1 = groupDto.getOptions().get(0);
        assertThat(optionDto1.getOptionName()).isEqualTo("톨(Tall)");
        assertThat(optionDto1.getOptionPrice()).isEqualTo(0L);
        assertThat(optionDto1.isSelected()).isFalse();

        UserOptionDto optionDto2 = groupDto.getOptions().get(1);
        assertThat(optionDto2.getOptionName()).isEqualTo("벤티(Venti)");
        assertThat(optionDto2.getOptionPrice()).isEqualTo(1000L);
        assertThat(optionDto2.isSelected()).isFalse();

        verify(menuRepository).findByMenuIdAndDeletedFalse(menuId);
        verify(menuImageRepository).findFirstByMenu_MenuIdOrderByDisplayOrderAsc(menuId);
        verify(menuOptionGroupRepository).findGroupsWithOptionsByMenuId(menuId);
    }
}
