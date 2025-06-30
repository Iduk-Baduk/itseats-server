package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreDto {
    private Long storeId;
    private String name;
    private double review;
    private int reviewCount;
    private List<String> images;
}
