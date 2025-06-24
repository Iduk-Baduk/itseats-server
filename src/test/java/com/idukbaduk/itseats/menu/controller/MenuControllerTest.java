package com.idukbaduk.itseats.menu.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.menu.dto.MenuGroupDto;
import com.idukbaduk.itseats.menu.dto.MenuGroupRequest;
import com.idukbaduk.itseats.menu.dto.MenuGroupResponse;
import com.idukbaduk.itseats.menu.dto.enums.MenuResponse;
import com.idukbaduk.itseats.menu.entity.Menu;
import com.idukbaduk.itseats.menu.entity.MenuGroup;
import com.idukbaduk.itseats.menu.service.MenuGroupService;
import com.idukbaduk.itseats.menu.service.MenuService;
import com.nimbusds.common.contenttype.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc(addFilters = false)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuService menuService;
    @MockitoBean
    private MenuGroupService menuGroupService;

    @DisplayName("메뉴 그룹 조회 성공")
    @Test
    void getMenuGroup_success() throws Exception {
        // given
        Long storeId = 1L;
        MenuGroupResponse response = MenuGroupResponse.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("음료", 1, true),
                        createMenuGroupDto("샌드위치", 999, false)
                )))
                .build();
        when(menuGroupService.getMenuGroup(storeId)).thenReturn(response);

        // when & then
        mockMvc.perform(get("/api/owner/" + storeId + "/menu-groups")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(MenuResponse.GET_MENU_GROUP_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menuGroups[0].displayName").value("음료"))
                .andExpect(jsonPath("$.data.menuGroups[1].menuGroupName").value("샌드위치"))
                .andExpect(jsonPath("$.data.menuGroups[1].displayName").value("샌드위치 (비활성화)"));
    }

    @Test
    @DisplayName("메뉴 그룹 설정 성공")
    void saveMenuGroup_success() throws Exception {
        // given
        MenuGroupResponse response = MenuGroupResponse.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("음료", 1, true)
                )))
                .build();
        when(menuGroupService.saveMenuGroup(any(), any())).thenReturn(response);

        Long storeId = 1L;
        MenuGroupRequest request = MenuGroupRequest.builder()
                .menuGroups(new ArrayList<>(List.of(
                        createMenuGroupDto("음료", 1, true)
                )))
                .build();

        // when & then
        mockMvc.perform(post("/api/owner/" + storeId + "/menu-groups")
                .content(objectMapper.writeValueAsString(request))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(MenuResponse.SAVE_MENU_GROUP_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menuGroups[0].menuGroupName").value("음료"));
    }

    private MenuGroupDto createMenuGroupDto(String groupName, int priority, boolean isActive) {
        return MenuGroupDto.builder()
                .menuGroupName(groupName)
                .menuGroupPriority(priority)
                .menuGroupIsActive(isActive)
                .build();
    }
}
