package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreCategoryListResponse {
    private String category;
    private String categoryName;
    private List<StoreDto> stores;
}
