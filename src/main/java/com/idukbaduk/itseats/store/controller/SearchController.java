package com.idukbaduk.itseats.store.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreResponse;
import com.idukbaduk.itseats.store.dto.enums.StoreSortOption;
import com.idukbaduk.itseats.store.service.StoreService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/search")
public class SearchController {

    private final StoreService storeService;

    @GetMapping("/stores/list")
    public ResponseEntity<BaseResponse> searchStores(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            @RequestParam(defaultValue = "ORDER_COUNT") StoreSortOption sort,
            @RequestParam(required = false) Long addressId
    ) {
        return BaseResponse.toResponseEntity(
                StoreResponse.SEARCH_STORES_SUCCESS,
                storeService.searchStores(userDetails.getUsername(), keyword, pageable, sort, addressId)
        );
    }
}
