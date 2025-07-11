package com.idukbaduk.itseats.menu.service;

import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.entity.*;
import com.idukbaduk.itseats.menu.entity.enums.MenuStatus;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.repository.MenuGroupRepository;
import com.idukbaduk.itseats.menu.repository.MenuImageRepository;
import com.idukbaduk.itseats.menu.repository.MenuRepository;
import com.idukbaduk.itseats.store.entity.Store;
import com.idukbaduk.itseats.store.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

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
    private MenuImageRepository menuImageRepository;
    @Mock
    private MenuMediaService menuMediaService;

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
        when(menuImageRepository.findByMenu_MenuIdInOrderByMenu_MenuIdAscDisplayOrderAsc(any()))
                .thenReturn(Collections.emptyList());

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
    @DisplayName("새로운 메뉴를 추가한다")
    void createMenu_success() {
        // given
        Long storeId = 1L;
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.ofNullable(MenuGroup.builder().menuGroupName("음료").build()));

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image content".getBytes());

        MenuRequest request = MenuRequest.builder()
                .menuName("아메리카노")
                .menuDescription("그냥 아메리카노")
                .menuPrice(2000L)
                .menuStatus(MenuStatus.ON_SALE)
                .menuGroupName("음료")
                .menuPriority(1)
                .optionGroups(List.of(
                        MenuOptionGroupDto.builder()
                                .optionGroupName("샷 추가")
                                .isRequired(false)
                                .minSelect(0)
                                .maxSelect(1)
                                .priority(1)
                                .options(List.of(
                                        MenuOptionDto.builder()
                                                .optionName("1번 샷 추가")
                                                .optionPrice(500L)
                                                .optionStatus(MenuStatus.ON_SALE)
                                                .optionPriority(1)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(menuMediaService.createMenuImages(any(Menu.class), any())).thenReturn(List.of(
                MenuImage.builder()
                        .imageUrl("s3 link")
                        .build()
        ));

        // when
        MenuResponse data = menuService.createMenu(storeId, request, List.of(imageFile));

        // then
        assertThat(data).isNotNull()
                .extracting("menuName", "menuStatus")
                .containsExactly("아메리카노", MenuStatus.ON_SALE);
        assertThat(data.getOptionGroups()).hasSize(1);
        assertThat(data.getOptionGroups().get(0).getOptions()).hasSize(1);
        assertThat(data.getImages()).hasSize(1)
                        .contains("s3 link");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 추가시 메뉴 그룹이 존재하지 않으면 예외 발생")
    void createMenu_menuGroupNotExists() {
        // given
        Long storeId = 1L;
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.empty());
        MenuRequest menuRequest = MenuRequest.builder()
                .menuGroupName("없는그룹")
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.createMenu(storeId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_GROUP_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메뉴 추가시 옵션 그룹명이 동일한게 있으면 예외 발생")
    void createMenu_optionGroupNameDuplicated() {
        // given
        Long storeId = 1L;
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.ofNullable(MenuGroup.builder().build()));
        MenuRequest menuRequest = MenuRequest.builder()
                .menuGroupName("그룹")
                .optionGroups(List.of(
                        MenuOptionGroupDto.builder().optionGroupName("사이드").build(),
                        MenuOptionGroupDto.builder().optionGroupName("사이드").build()
                        ))
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.createMenu(storeId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.OPTION_GROUP_NAME_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("메뉴 추가시 옵션 최소 선택이 최대 선택보다 크면 예외 발생")
    void createMenu_optionGroupRangeInvalid() {
        // given
        Long storeId = 1L;
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.ofNullable(MenuGroup.builder().build()));
        MenuRequest menuRequest = MenuRequest.builder()
                .menuGroupName("그룹")
                .optionGroups(List.of(
                        MenuOptionGroupDto.builder().optionGroupName("사이드").maxSelect(1).minSelect(2).build()
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.createMenu(storeId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.OPTION_GROUP_RANGE_INVALID.getMessage());
    }

    @Test
    @DisplayName("기존의 메뉴를 수정한다")
    void updateMenu_success() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.of(MenuGroup.builder().menuGroupName("음료").build()));

        MenuOption option = MenuOption.builder()
                .optionName("커피번")
                .optionPrice(3000L)
                .build();
        MenuOptionGroup optionGroup = MenuOptionGroup.builder()
                .optGroupName("사이드")
                .options(List.of(option))
                .build();
        Menu menu = Menu.builder()
                .menuName("아메리카노")
                .menuStatus(MenuStatus.ON_SALE)
                .menuPrice(2000L)
                .menuOptionGroups(new ArrayList<>(List.of(optionGroup)))
                .build();

        when(menuRepository.findByStoreIdAndMenuId(storeId, menuId)).thenReturn(Optional.of(menu));

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image content".getBytes());

        MenuRequest request = MenuRequest.builder()
                .menuName("카페라떼")
                .menuPrice(3000L)
                .menuStatus(MenuStatus.HIDDEN)
                .menuGroupName("음료")
                .optionGroups(List.of(
                        MenuOptionGroupDto.builder()
                                .optionGroupName("샷 추가")
                                .isRequired(false)
                                .minSelect(0)
                                .maxSelect(1)
                                .priority(1)
                                .options(List.of(
                                        MenuOptionDto.builder()
                                                .optionName("1번 샷 추가")
                                                .optionPrice(500L)
                                                .optionStatus(MenuStatus.ON_SALE)
                                                .optionPriority(1)
                                                .build()
                                ))
                                .build()
                ))
                .build();
        when(menuRepository.save(any(Menu.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(menuMediaService.updateMenuImages(any(Menu.class), any())).thenReturn(List.of(
                MenuImage.builder()
                        .imageUrl("s3 link")
                        .build()
        ));

        // when
        MenuResponse data = menuService.updateMenu(storeId, menuId, request, List.of(imageFile));

        // then
        assertThat(data).isNotNull()
                .extracting("menuName", "menuPrice", "menuStatus")
                .containsExactly("카페라떼", 3000L, MenuStatus.HIDDEN);
        assertThat(data.getOptionGroups()).hasSize(1);
        assertThat(data.getOptionGroups().get(0).getOptionGroupName()).isEqualTo("샷 추가");
        assertThat(data.getOptionGroups().get(0).getOptions()).hasSize(1);
        assertThat(data.getOptionGroups().get(0).getOptions().get(0).getOptionName()).isEqualTo("1번 샷 추가");
        assertThat(data.getImages()).hasSize(1)
                .contains("s3 link");
        verify(menuRepository).save(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 수정시 메뉴가 존재하지 않으면 예외 발생")
    void updateMenu_notFound() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;
        when(menuRepository.findByStoreIdAndMenuId(storeId, menuId))
                .thenReturn(Optional.empty());
        MenuRequest menuRequest = MenuRequest.builder()
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.updateMenu(storeId, menuId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메뉴 수정시 메뉴 그룹이 존재하지 않으면 예외 발생")
    void updateMenu_menuGroupNotExists() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;
        when(menuRepository.findByStoreIdAndMenuId(storeId, menuId))
                .thenReturn(Optional.of(Menu.builder().build()));
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.empty());
        MenuRequest menuRequest = MenuRequest.builder()
                .menuGroupName("없는그룹")
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.updateMenu(storeId, menuId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_GROUP_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메뉴 수정시 옵션 그룹명이 동일한게 있으면 예외 발생")
    void updateMenu_optionGroupNameDuplicated() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;
        when(menuRepository.findByStoreIdAndMenuId(storeId, menuId))
                .thenReturn(Optional.of(Menu.builder().build()));
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.of(MenuGroup.builder().menuGroupName("그룹").build()));

        MenuRequest menuRequest = MenuRequest.builder()
                .menuGroupName("그룹")
                .optionGroups(List.of(
                        MenuOptionGroupDto.builder().optionGroupName("사이드").build(),
                        MenuOptionGroupDto.builder().optionGroupName("사이드").build()
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.updateMenu(storeId, menuId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.OPTION_GROUP_NAME_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("메뉴 수정시 옵션 최소 선택이 최대 선택보다 크면 예외 발생")
    void updateMenu_optionGroupRangeInvalid() {
        // given
        Long storeId = 1L;
        Long menuId = 1L;
        when(menuRepository.findByStoreIdAndMenuId(storeId, menuId))
                .thenReturn(Optional.of(Menu.builder().build()));
        when(menuGroupRepository.findMenuGroupByMenuGroupNameAndStore_StoreId(any(), any()))
                .thenReturn(Optional.of(MenuGroup.builder().menuGroupName("그룹").build()));

        MenuRequest menuRequest = MenuRequest.builder()
                .menuGroupName("그룹")
                .optionGroups(List.of(
                        MenuOptionGroupDto.builder().optionGroupName("사이드").maxSelect(1).minSelect(2).build()
                ))
                .build();

        // when & then
        assertThatThrownBy(() -> menuService.updateMenu(storeId, menuId, menuRequest, Collections.emptyList()))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.OPTION_GROUP_RANGE_INVALID.getMessage());
    }

    @Test
    @DisplayName("기존의 메뉴를 삭제 한다")
    void deleteMenu_success() {
        // given
        when(menuRepository.findByStoreIdAndMenuId(any(), any())).thenReturn(Optional.of(
                Menu.builder().menuId(1L).build())
        );

        // when
        menuService.deleteMenu(1L, 1L);

        // then
        verify(menuRepository).delete(any(Menu.class));
    }

    @Test
    @DisplayName("메뉴 삭제시 메뉴가 존재하지 않으면 예외 발생")
    void deleteMenu_notFound() {
        // given
        when(menuRepository.findByStoreIdAndMenuId(any(), any())).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->menuService.deleteMenu(1L, 1L))
                .isInstanceOf(MenuException.class)
                .hasMessageContaining(MenuErrorCode.MENU_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("메뉴의 우선순위를 변경한다")
    void updateMenuPriority_success() {
        // given
        Long storeId = 1L;
        MenuGroup menuGroup = MenuGroup.builder().menuGroupId(1L).build();
        List<Menu> menus = new ArrayList<>(List.of(
                Menu.builder().menuId(1L).menuPriority(1).menuStatus(MenuStatus.ON_SALE).menuGroup(menuGroup).build(),
                Menu.builder().menuId(2L).menuPriority(2).menuStatus(MenuStatus.ON_SALE).menuGroup(menuGroup).build()
        ));
        when(menuRepository.findByStoreId(storeId)).thenReturn(menus);
        when(menuImageRepository.findByMenu_MenuIdInOrderByMenu_MenuIdAscDisplayOrderAsc(any()))
                .thenReturn(Collections.emptyList());

        List<MenuInfoDto> menuInfoDtos = List.of(
                MenuInfoDto.builder().menuId(1L).menuPriority(2).build(),
                MenuInfoDto.builder().menuId(2L).menuPriority(1).build()
        );
        MenuPriorityRequest request = MenuPriorityRequest.builder()
                .menus(menuInfoDtos)
                .build();

        // when
        MenuListResponse data = menuService.updateMenuPriority(storeId, request);

        // then
        assertThat(data.getMenus()).hasSize(2)
                .extracting("menuId", "menuPriority")
                .containsExactly(
                        tuple(2L, 1),
                        tuple(1L, 2)
                );
    }
}
