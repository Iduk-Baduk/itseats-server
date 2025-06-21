package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.StoreCategoryListResponse;
import com.idukbaduk.itseats.store.dto.StoreListResponse;
import com.idukbaduk.itseats.store.service.StoreService;
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
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> getAllStores() {
        StoreListResponse response = storeService.getAllStores();
        return BaseResponse.toResponseEntity(HttpStatus.OK, "전체 가게 목록 조회 성공", response);
    }

    @GetMapping("/list/{storeCategory}")
    public ResponseEntity<BaseResponse> getStoresByCategory(@PathVariable String storeCategory) {
        StoreCategoryListResponse response = storeService.getStoresByCategory(storeCategory);
        return BaseResponse.toResponseEntity(HttpStatus.OK, "카테고리 별 가게 목록 조회 성공", response);
    }
}
