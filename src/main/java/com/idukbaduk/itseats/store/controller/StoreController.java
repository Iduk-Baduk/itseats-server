package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.StoreDetailResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreSortOption;
import com.idukbaduk.itseats.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stores")
public class StoreController {

    private static final Logger log = LoggerFactory.getLogger(StoreController.class);

    private final StoreService storeService;

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> getAllStores(@PageableDefault(size = 20) Pageable pageable) {
        return BaseResponse.toResponseEntity(
                StoreResponse.GET_STORES_SUCCESS,
                storeService.getAllStores(pageable)
        );
    }

    @GetMapping("/{storeId}")
    public ResponseEntity<BaseResponse> getStoreDetail(
            @PathVariable Long storeId,
            @AuthenticationPrincipal UserDetails userDetails
            ) {
        log.info("매장 상세 조회 요청 시작 - storeId: {}, username: {}", storeId, 
                userDetails != null ? userDetails.getUsername() : "null");
        
        try {
            StoreDetailResponse response = storeService.getStoreDetail(userDetails.getUsername(), storeId);
            log.info("매장 상세 조회 성공 - storeId: {}, storeName: {}", storeId, response.getName());
            return BaseResponse.toResponseEntity(
                    StoreResponse.GET_STORE_DETAIL_SUCCESS,
                    response
            );
        } catch (Exception e) {
            log.error("매장 상세 조회 실패 - storeId: {}, username: {}, error: {}", 
                    storeId, userDetails != null ? userDetails.getUsername() : "null", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/list/{storeCategory}")
    public ResponseEntity<BaseResponse> getStoresByCategory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String storeCategory,
            @PageableDefault Pageable pageable,
            @RequestParam(defaultValue = "ORDER_COUNT") StoreSortOption sort,
            @RequestParam(required = false) Long addressId
            ) {
        return BaseResponse.toResponseEntity(
                StoreResponse.GET_STORES_BY_CATEGORY_SUCCESS,
                storeService.getStoresByCategory(
                        (userDetails == null ? null : userDetails.getUsername()),
                        storeCategory, pageable,
                        sort,
                        addressId
                )
        );
    }
}
