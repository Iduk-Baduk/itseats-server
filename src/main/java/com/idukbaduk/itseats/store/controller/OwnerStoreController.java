package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.*;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

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
            @ModelAttribute StoreCreateRequest request
    ) {
        StoreCreateResponse response = ownerStoreService.createStore(userDetails.getUsername(), request);
        return BaseResponse.toResponseEntity(StoreResponse.CREATE_STORE_SUCCESS, response);
    }

    @PostMapping("/stores/{storeId}/status")
    public ResponseEntity<BaseResponse> updateStatus(
            @PathVariable("storeId") Long storeId,
            @RequestBody StoreStatusUpdateRequest request
    ) {
        StoreStatusUpdateResponse response = ownerStoreService.updateStatus(storeId, request);
        return BaseResponse.toResponseEntity(StoreResponse.UPDATE_STATUS_SUCCESS, response);
    }
}
