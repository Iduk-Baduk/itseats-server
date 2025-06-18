package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.UserMenuListResponse;
import com.idukbaduk.itseats.menu.service.UserService;
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
public class UserController {

    private final UserService userService;

    @GetMapping("/{storeId}/menus")
    public ResponseEntity<BaseResponse> getMenus(
            @PathVariable("store_id") Long storeId) {
        UserMenuListResponse response = userService.getMenusByStore(storeId);
        return BaseResponse.toResponseEntity(HttpStatus.OK, "메뉴 조회 성공", response);
    }
}
