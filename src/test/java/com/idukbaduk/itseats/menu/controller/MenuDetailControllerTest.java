package com.idukbaduk.itseats.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.controller.MenuController;
import com.idukbaduk.itseats.menu.dto.MenuDetailResponse;
import com.idukbaduk.itseats.menu.dto.MenuOptionDto;
import com.idukbaduk.itseats.menu.dto.MenuOptionGroupDto;
import com.idukbaduk.itseats.menu.dto.enums.MenuResponse;
import com.idukbaduk.itseats.menu.error.MenuErrorCode;
import com.idukbaduk.itseats.menu.error.MenuException;
import com.idukbaduk.itseats.menu.service.MenuGroupService;
import com.idukbaduk.itseats.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.BDDMockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuDetailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MenuService menuService;

    @MockitoBean
    private MenuGroupService menuGroupService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("메뉴 상세 조회 성공 응답")
    void getMenuDetail_success() throws Exception {
        // given
        Long storeId = 1L;
        Long menuId = 11L;
        MenuOptionDto optionDto = MenuOptionDto.builder()
                .optionName("톨(Tall)")
                .optionPrice(0L)
                .optionStatus(com.idukbaduk.itseats.menu.entity.enums.MenuStatus.ON_SALE)
                .optionPriority(1)
                .build();
        MenuOptionGroupDto groupDto = MenuOptionGroupDto.builder()
                .optionGroupName("사이즈")
                .isRequired(true)
                .minSelect(1)
                .maxSelect(1)
                .priority(1)
                .options(List.of(optionDto))
                .build();
        MenuDetailResponse response = MenuDetailResponse.builder()
                .menuId(menuId)
                .menuName("아메리카노")
                .menuDescription("평범한 아메리카노입니다.")
                .menuPrice(2000L)
                .menuStatus("ON_SALE")
                .menuRating(4.43f)
                .menuGroupName("음료")
                .optionGroups(List.of(groupDto))
                .build();

        given(menuService.getMenuDetail(storeId, menuId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/owner/{storeId}/menus/{menuId}", storeId, menuId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(MenuResponse.GET_MENU_DETAIL_SUCCESS.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(MenuResponse.GET_MENU_DETAIL_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menuId").value(menuId))
                .andExpect(jsonPath("$.data.menuName").value("아메리카노"))
                .andExpect(jsonPath("$.data.menuDescription").value("평범한 아메리카노입니다."))
                .andExpect(jsonPath("$.data.menuPrice").value(2000))
                .andExpect(jsonPath("$.data.menuStatus").value("ON_SALE"))
                .andExpect(jsonPath("$.data.menuRating").value(4.43f))
                .andExpect(jsonPath("$.data.menuGroupName").value("음료"))
                .andExpect(jsonPath("$.data.optionGroups[0].optionGroupName").value("사이즈"))
                .andExpect(jsonPath("$.data.optionGroups[0].required").value(true))
                .andExpect(jsonPath("$.data.optionGroups[0].minSelect").value(1))
                .andExpect(jsonPath("$.data.optionGroups[0].maxSelect").value(1))
                .andExpect(jsonPath("$.data.optionGroups[0].priority").value(1))
                .andExpect(jsonPath("$.data.optionGroups[0].options[0].optionName").value("톨(Tall)"))
                .andExpect(jsonPath("$.data.optionGroups[0].options[0].optionPrice").value(0))
                .andExpect(jsonPath("$.data.optionGroups[0].options[0].optionStatus").value("ON_SALE"))
                .andExpect(jsonPath("$.data.optionGroups[0].options[0].optionPriority").value(1));
    }

    @Test
    @DisplayName("메뉴가 없으면 404 에러 응답")
    void getMenuDetail_menuNotFound() throws Exception {
        // given
        Long storeId = 1L;
        Long menuId = 999L;
        given(menuService.getMenuDetail(storeId, menuId))
                .willThrow(new MenuException(MenuErrorCode.MENU_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/api/owner/{storeId}/menus/{menuId}", storeId, menuId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(MenuErrorCode.MENU_NOT_FOUND.getHttpStatus().value()))
                .andExpect(jsonPath("$.message").value(MenuErrorCode.MENU_NOT_FOUND.getMessage()));
    }
}
