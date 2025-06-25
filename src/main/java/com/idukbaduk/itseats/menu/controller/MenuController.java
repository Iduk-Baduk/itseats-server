package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.MenuGroupRequest;
import com.idukbaduk.itseats.menu.dto.MenuGroupResponse;
import com.idukbaduk.itseats.menu.dto.MenuListRequest;
import com.idukbaduk.itseats.menu.dto.MenuRequest;
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
            @Valid @RequestPart("request") MenuRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return BaseResponse.toResponseEntity(
                MenuResponse.CREATE_MENU_SUCCESS,
                menuService.createMenu(storeId, request, images)
        );
    }

    @PutMapping(value = "/{storeId}/menus/{menuId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BaseResponse> updateMenu(
            @PathVariable Long storeId,
            @PathVariable Long menuId,
            @Valid @RequestPart("request") MenuRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return BaseResponse.toResponseEntity(
                MenuResponse.UPDATE_MENU_SUCCESS,
                menuService.updateMenu(storeId, menuId, request, images)
        );
    }

    @DeleteMapping(value = "/{storeId}/menus/{menuId}")
    public ResponseEntity<BaseResponse> deleteMenu(@PathVariable Long storeId, @PathVariable Long menuId) {
        menuService.deleteMenu(menuId);
        return BaseResponse.toResponseEntity(MenuResponse.DELETE_MENU_SUCCESS);
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
