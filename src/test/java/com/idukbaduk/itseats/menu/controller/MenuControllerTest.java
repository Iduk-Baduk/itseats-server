package com.idukbaduk.itseats.menu.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.service.MenuGroupService;
import com.idukbaduk.itseats.menu.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.idukbaduk.itseats.menu.dto.enums.MenuResponse.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "spring.config.import=optional:classpath:application-test.yml"
})
@Import(MenuControllerTest.TestConfig.class)
class MenuControllerTest {

    @TestConfiguration
    static class TestConfig {
        @Bean
        public String jwtSecret() {
            return "85a25e195b4ab0e8066784a48070334a0aa0cd482304c7b7b9f20b46664a8af46ee6480aaedefd35f02721ab3157baa6de748cdde8b108bfc7eba804f057838c";
        }
    }

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuService menuService;
    @MockitoBean
    private MenuGroupService menuGroupService;

    @DisplayName("메뉴 추가 성공")
    @Test
    void createMenu_success() throws Exception {
        // given
        MenuResponse response = MenuResponse.builder()
                .menuId(1L)
                .menuName("아메리카노")
                .menuPrice(2000L)
                .menuGroupName("음료")
                .images(List.of("s3 link"))
                .build();
        when(menuService.createMenu(anyLong(), any(), any())).thenReturn(response);

        String requestJson = """
            {
                "menuName": "아메리카노",
                "menuDescription": "평범한 아메리카노입니다.",
                "menuPrice": 2000,
                "menuStatus": "ON_SALE",
                "menuGroupName": "음료",
                "menuPriority": 1
            }
        """;
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image content".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/owner/1/menus/new")
                        .file(requestPart)
                        .file(imageFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.httpStatus").value(201))
                .andExpect(jsonPath("$.message").value(CREATE_MENU_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menuId").value("1"))
                .andExpect(jsonPath("$.data.menuName").value("아메리카노"));
    }

    @DisplayName("메뉴 추가 요청 검증 실패 - 메뉴 이름 없음")
    @Test
    void createMenu_notValid() throws Exception {
        // given
        String requestJson = """
            {
                "menuDescription": "평범한 아메리카노입니다.",
                "menuPrice": 2000,
                "menuStatus": "ON_SALE",
                "menuGroupName": "음료",
                "menuPriority": 1
            }
        """;
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );

        // when & then
        mockMvc.perform(multipart("/api/owner/1/menus/new")
                        .file(requestPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("메뉴 이름은 필수입니다."));
    }

    @DisplayName("메뉴 수정 성공")
    @Test
    void updateMenu_success() throws Exception {
        // given
        MenuResponse response = MenuResponse.builder()
                .menuId(1L)
                .menuName("카페라떼")
                .menuPrice(3000L)
                .menuGroupName("음료")
                .images(List.of("s3 link"))
                .build();
        when(menuService.updateMenu(anyLong(), anyLong(), any(), any())).thenReturn(response);

        String requestJson = """
            {
                "menuName": "카페라떼",
                "menuDescription": "평범한 카페라떼입니다.",
                "menuPrice": 3000,
                "menuStatus": "HIDDEN",
                "menuGroupName": "음료",
                "menuPriority": 1
            }
        """;
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );

        MockMultipartFile imageFile = new MockMultipartFile(
                "images", "test.jpg", "image/jpeg", "test image content".getBytes());

        // when & then
        mockMvc.perform(multipart("/api/owner/1/menus/1")
                        .file(requestPart)
                        .file(imageFile)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(UPDATE_MENU_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menuId").value("1"))
                .andExpect(jsonPath("$.data.menuName").value("카페라떼"));
    }

    @DisplayName("메뉴 수정 요청 검증 실패 - 가격이 음수")
    @Test
    void updateMenu_notValid() throws Exception {
        // when & then
        String requestJson = """
        {
            "menuName": "카페라떼",
            "menuDescription": "평범한 카페라떼입니다.",
            "menuPrice": -3000,
            "menuStatus": "ON_SALE",
            "menuGroupName": "음료",
            "menuPriority": 1
        }
        """;
        MockMultipartFile requestPart = new MockMultipartFile(
                "request", "request.json", "application/json", requestJson.getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/api/owner/1/menus/1")
                        .file(requestPart)
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .accept(MediaType.APPLICATION_JSON)
                        .characterEncoding("UTF-8"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("메뉴 가격은 0원 이상이어야 합니다."));
    }

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
                .andExpect(jsonPath("$.message").value(GET_MENU_GROUP_SUCCESS.getMessage()))
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
                .andExpect(jsonPath("$.message").value(SAVE_MENU_GROUP_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menuGroups[0].menuGroupName").value("음료"));
    }

    @Test
    @DisplayName("메뉴 순서 설정 성공")
    void updateMenuPriority_success() throws Exception {
        // given
        List<MenuInfoDto> menus = List.of(
                MenuInfoDto.builder().menuId(2L).menuPriority(1).build(),
                MenuInfoDto.builder().menuId(1L).menuPriority(2).build()
        );
        MenuListResponse response = MenuListResponse.builder()
                .totalMenuCount(2)
                .orderableMenuCount(1)
                .outOfStockTodayCount(1)
                .hiddenMenuCount(0)
                .menus(menus)
                .build();
        when(menuService.updateMenuPriority(any(), any())).thenReturn(response);

        Long storeId = 1L;
        List<MenuInfoDto> menuInfoDtos = List.of(
                MenuInfoDto.builder().menuId(1L).menuPriority(2).build(),
                MenuInfoDto.builder().menuId(2L).menuPriority(1).build()
        );
        MenuPriorityRequest request = MenuPriorityRequest.builder()
                .menus(menuInfoDtos)
                .build();

        // when & then
        mockMvc.perform(put("/api/owner/" + storeId + "/menus/priority")
                        .content(objectMapper.writeValueAsString(request))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.httpStatus").value(200))
                .andExpect(jsonPath("$.message").value(SET_MENU_ORDER_SUCCESS.getMessage()))
                .andExpect(jsonPath("$.data.menus[0].menuId").value("2"))
                .andExpect(jsonPath("$.data.menus[1].menuId").value("1"));
    }

    private MenuGroupDto createMenuGroupDto(String groupName, int priority, boolean isActive) {
        return MenuGroupDto.builder()
                .menuGroupName(groupName)
                .menuGroupPriority(priority)
                .menuGroupIsActive(isActive)
                .build();
    }
}
