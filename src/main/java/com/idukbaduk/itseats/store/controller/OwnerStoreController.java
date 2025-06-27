package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.StoreDashboardResponse;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.idukbaduk.itseats.store.dto.StoreCreateRequest;
import com.idukbaduk.itseats.store.dto.StoreCreateResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner")
public class OwnerStoreController {

    private final OwnerStoreService ownerStoreService;

    @GetMapping("/{storeId}/dashboard")
    public ResponseEntity<BaseResponse> getStoreDashboard(@PathVariable Long storeId) {
        StoreDashboardResponse response = ownerStoreService.getDashboard(storeId);
        return BaseResponse.toResponseEntity(HttpStatus.OK, "가맹점 대시보드 조회 성공", response);
    }

    @PostMapping(value = "/store-regist", consumes = {"multipart/form-data"})
    public ResponseEntity<BaseResponse> createStore(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestPart StoreCreateRequest request,
            @RequestPart(required = false) List<MultipartFile> images
    ) {
        StoreCreateResponse response = ownerStoreService.createStore(userDetails.getUsername(), request, images);
        return BaseResponse.toResponseEntity(StoreResponse.CREATE_STORE_SUCCESS, response);
    }
}
