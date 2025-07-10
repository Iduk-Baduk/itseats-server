package com.idukbaduk.itseats.member.dto;

import com.idukbaduk.itseats.store.entity.Store;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreIdAndStoreNameDto {

    private Long storeId;
    private String storeName;

    public static StoreIdAndStoreNameDto of(Store store) {
        return StoreIdAndStoreNameDto.builder()
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .build();
    }
}
