package com.idukbaduk.itseats.store.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class StoreCreateResponse {
    private Long storeId;
    private String name;
    private String categoryName;
    private boolean isFranchise;
    private String description;
    private String address;
    private String phone;
}
