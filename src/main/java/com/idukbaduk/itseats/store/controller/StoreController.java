package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.store.dto.StoreDetailResponse;
import com.idukbaduk.itseats.store.dto.StoreListResponse;
import com.idukbaduk.itseats.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> getAllStores() {
        StoreListResponse response = storeService.getAllStores();
        return BaseResponse.toResponseEntity(HttpStatus.OK, "전체 가게 목록 조회 성공", response);
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<BaseResponse> getStoreDetail(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        StoreDetailResponse response = storeService.getStoreDetail(userDetails.getUsername(), storeId);
        return BaseResponse.toResponseEntity(HttpStatus.OK, "가게 상세 조회 성공", response);
    }

}
