package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.*;
import com.idukbaduk.itseats.menu.dto.enums.MenuResponse;
import com.idukbaduk.itseats.menu.service.MenuGroupService;
import com.idukbaduk.itseats.menu.service.MenuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/owner")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    private final MenuGroupService menuGroupService;

    @PostMapping("/{storeId}/menus")
    public ResponseEntity<BaseResponse> getMenuList(
            @PathVariable Long storeId,
            @RequestBody MenuListRequest request
    ) {
        return BaseResponse.toResponseEntity(
                MenuResponse.GET_MENU_LIST_SUCCESS,
                menuService.getMenuList(storeId, request)
        );
    }

    @PostMapping(value = "/{storeId}/menus/new", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> createMenu(
            @PathVariable Long storeId,
            @Valid @ModelAttribute MenuRequest request
            ) {
        // TODO 이미지 전송 엔드포인트 분리

        return BaseResponse.toResponseEntity(
                MenuResponse.CREATE_MENU_SUCCESS,
                menuService.createMenu(storeId, request)
        );
    }

    @PostMapping("/{storeId}/menu-groups")
    public ResponseEntity<BaseResponse> saveMenuGroup(
            @PathVariable Long storeId,
            @RequestBody MenuGroupRequest request
    ) {
        return BaseResponse.toResponseEntity(
                MenuResponse.SAVE_MENU_GROUP_SUCCESS,
                menuGroupService.saveMenuGroup(storeId, request)
        );
    }

    @GetMapping("/{storeId}/menu-groups")
    public ResponseEntity<BaseResponse> getMenuGroup(@PathVariable Long storeId) {
        MenuGroupResponse data = menuGroupService.getMenuGroup(storeId);
        return BaseResponse.toResponseEntity(MenuResponse.GET_MENU_GROUP_SUCCESS, data);
    }
}
