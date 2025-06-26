package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.MenuDetailResponse;
import com.idukbaduk.itseats.menu.dto.MenuOptionDto;
import com.idukbaduk.itseats.menu.dto.MenuOptionGroupDto;
import com.idukbaduk.itseats.menu.entity.*;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.repository.MenuRepository;

import com.idukbaduk.itseats.store.entity.Store;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MenuDetailServiceTest {

    @Mock
    private MenuRepository menuRepository;

    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("메뉴 상세 조회 성공")
    void getMenuDetail_success() {
        // given
        Long storeId = 1L;
        Long menuId = 11L;

        Store store = Store.builder().storeId(storeId).build();
        MenuGroup menuGroup = MenuGroup.builder().menuGroupId(1L).menuGroupName("음료").store(store).build();
        MenuOption option1 = MenuOption.builder()
                .optionId(1L).optionName("톨(Tall)").optionPrice(0L).optionStatus(MenuStatus.ON_SALE).optionPriority(1)
                .build();
        MenuOption option2 = MenuOption.builder()
                .optionId(2L).optionName("벤티(Venti)").optionPrice(1000L).optionStatus(MenuStatus.HIDDEN).optionPriority(2)
                .build();
        MenuOptionGroup group = MenuOptionGroup.builder()
                .optGroupId(1L).optGroupName("사이즈").isRequired(true).minSelect(1).maxSelect(1).optGroupPriority(1)
                .options(List.of(option1, option2))
                .build();
        Menu menu = Menu.builder()
                .menuId(menuId)
                .menuName("아메리카노")
                .menuDescription("평범한 아메리카노입니다.")
                .menuPrice(2000L)
                .menuStatus(MenuStatus.ON_SALE)
                .menuRating(4.43f)
                .menuGroup(menuGroup)
                .menuOptionGroups(List.of(group))
                .build();

        given(menuRepository.findDetailById(menuId)).willReturn(Optional.of(menu));

        // when
        MenuDetailResponse response = menuService.getMenuDetail(storeId, menuId);

        // then
        assertThat(response.getMenuId()).isEqualTo(menuId);
        assertThat(response.getMenuName()).isEqualTo("아메리카노");
        assertThat(response.getMenuDescription()).isEqualTo("평범한 아메리카노입니다.");
        assertThat(response.getMenuPrice()).isEqualTo(2000L);
        assertThat(response.getMenuStatus()).isEqualTo("ON_SALE");
        assertThat(response.getMenuRating()).isEqualTo(4.43f);
        assertThat(response.getMenuGroupName()).isEqualTo("음료");
        assertThat(response.getOptionGroups()).hasSize(1);
        MenuOptionGroupDto groupDto = response.getOptionGroups().get(0);
        assertThat(groupDto.getOptionGroupName()).isEqualTo("사이즈");
        assertThat(groupDto.isRequired()).isTrue();
        assertThat(groupDto.getMinSelect()).isEqualTo(1);
        assertThat(groupDto.getMaxSelect()).isEqualTo(1);
        assertThat(groupDto.getPriority()).isEqualTo(1);
        assertThat(groupDto.getOptions()).hasSize(2);
        MenuOptionDto opt1 = groupDto.getOptions().get(0);
        assertThat(opt1.getOptionName()).isEqualTo("톨(Tall)");
        assertThat(opt1.getOptionPrice()).isEqualTo(0L);
        assertThat(opt1.getOptionStatus()).isEqualTo(MenuStatus.ON_SALE);
        assertThat(opt1.getOptionPriority()).isEqualTo(1);
        MenuOptionDto opt2 = groupDto.getOptions().get(1);
        assertThat(opt2.getOptionName()).isEqualTo("벤티(Venti)");
        assertThat(opt2.getOptionPrice()).isEqualTo(1000L);
        assertThat(opt2.getOptionStatus()).isEqualTo(MenuStatus.HIDDEN);
        assertThat(opt2.getOptionPriority()).isEqualTo(2);
    }

    @Test
    @DisplayName("메뉴가 없으면 예외 발생")
    void getMenuDetail_menuNotFound() {
        // given
        Long storeId = 1L;
        Long menuId = 999L;
        given(menuRepository.findDetailById(menuId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> menuService.getMenuDetail(storeId, menuId))
                .isInstanceOf(MenuException.class)
                .hasMessage(MenuErrorCode.MENU_NOT_FOUND.getMessage());
    }
}
