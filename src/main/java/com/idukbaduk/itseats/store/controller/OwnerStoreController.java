package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.StoreDashboardResponse;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.idukbaduk.itseats.store.dto.StoreCreateRequest;
import com.idukbaduk.itseats.store.dto.StoreCreateResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.service.OwnerStoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
