package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.MenuGroupRequest;
import com.idukbaduk.itseats.menu.dto.MenuGroupResponse;
import com.idukbaduk.itseats.menu.dto.MenuListRequest;
import com.idukbaduk.itseats.menu.dto.MenuListResponse;
import com.idukbaduk.itseats.menu.dto.enums.MenuResponse;
import com.idukbaduk.itseats.menu.service.MenuGroupService;
import com.idukbaduk.itseats.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        MenuListResponse data = menuService.getMenuList(storeId, request);
        return BaseResponse.toResponseEntity(MenuResponse.GET_MENU_LIST_SUCCESS, data);
    }

    @PostMapping("/{storeId}/menu-groups")
    public ResponseEntity<BaseResponse> saveMenuGroup(
            @PathVariable Long storeId,
            @RequestBody MenuGroupRequest request
    ) {
        MenuGroupResponse data = menuGroupService.saveMenuGroup(storeId, request);
        return BaseResponse.toResponseEntity(MenuResponse.SAVE_MENU_GROUP_SUCCESS, data);
    }

    @GetMapping("/{storeId}/menu-groups")
    public ResponseEntity<BaseResponse> getMenuGroup(@PathVariable Long storeId) {
        MenuGroupResponse data = menuGroupService.getMenuGroup(storeId);
        return BaseResponse.toResponseEntity(MenuResponse.GET_MENU_GROUP_SUCCESS, data);
    }
}
