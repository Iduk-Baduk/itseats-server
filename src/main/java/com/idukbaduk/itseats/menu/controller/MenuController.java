package com.idukbaduk.itseats.menu.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.menu.dto.MenuListRequest;
import com.idukbaduk.itseats.menu.dto.MenuListResponse;
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

    @PostMapping("/{storeId}/menus")
    public ResponseEntity<BaseResponse> getMenuList(
            @PathVariable Long storeId,
            @RequestBody MenuListRequest request
    ) {
        MenuListResponse data = menuService.getMenuList(storeId, request);
        return BaseResponse.toResponseEntity(HttpStatus.OK,"메뉴 목록 조회 성공", data);
    }

}
