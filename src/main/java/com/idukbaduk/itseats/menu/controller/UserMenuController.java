package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.UserMenuOptionResponse;
import com.idukbaduk.itseats.menu.dto.UserMenuListResponse;
import com.idukbaduk.itseats.menu.service.UserMenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class UserMenuController {

    private final UserMenuService userMenuService;

    @GetMapping("/{storeId}/{menuId}/options")
    public ResponseEntity<BaseResponse> getMenuOptions(
            @PathVariable Long storeId,
            @PathVariable Long menuId
    ) {
        UserMenuOptionResponse response = userMenuService.getMenuOptions(storeId, menuId);

        return BaseResponse.toResponseEntity(HttpStatus.OK, "메뉴 옵션 조회 성공", response);
    }

    @GetMapping("/{storeId}/menus")
    public ResponseEntity<BaseResponse> getMenus(
            @PathVariable Long storeId)
        {
        UserMenuListResponse response = userMenuService.getMenusByStore(storeId);
      
        return BaseResponse.toResponseEntity(HttpStatus.OK, "메뉴 조회 성공", response);
    }
}
