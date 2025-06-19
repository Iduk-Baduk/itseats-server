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
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import com.idukbaduk.itseats.menu.repository.MenuOptionGroupRepository;
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

    @DisplayName("메뉴 옵션 조회 성공")
    @Test
    void getMenuOptions_success() {
        // given
        Long menuId = 1L;

        MenuGroup menuGroup = MenuGroup.builder()
                .menuGroupId(10L)
                .menuGroupName("음료")
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
        UserMenuOptionResponse response = userMenuService.getMenuOptions(menuId);

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
