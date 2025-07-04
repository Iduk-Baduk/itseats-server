package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreListResponse {
    private List<StoreDto> stores;
    private Integer currentPage;
    private Boolean hasNext;
}
