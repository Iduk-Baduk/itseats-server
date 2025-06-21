package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreDto {
    private String imageUrl;
    private String name;
    private double review;
    private int reviewCount;
}
