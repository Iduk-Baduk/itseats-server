package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.MenuGroupRequest;
import com.idukbaduk.itseats.menu.dto.MenuGroupResponse;
import com.idukbaduk.itseats.menu.dto.MenuListRequest;
import com.idukbaduk.itseats.menu.dto.MenuListResponse;
import com.idukbaduk.itseats.menu.service.MenuGroupService;
import com.idukbaduk.itseats.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
        return BaseResponse.toResponseEntity(HttpStatus.OK,"메뉴 목록 조회 성공", data);
    }

    @PostMapping("/{storeId}/menu-groups")
    public ResponseEntity<BaseResponse> saveMenuGroup(
            @PathVariable Long storeId,
            @RequestBody MenuGroupRequest request
    ) {
        MenuGroupResponse data = menuGroupService.saveMenuGroup(storeId, request);
        return BaseResponse.toResponseEntity(HttpStatus.OK, "메뉴 그룹 설정 성공", data);
    }
}
