package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class StoreDetailResponse {
    private String name;
    private boolean isLiked;
    private double review;
    private int reviewCount;
    private List<String> images;
    private String description;
    private String address;
    private String phone;
    private int defaultDeliveryFee;
    private int onlyOneDeliveryFee;
    private boolean isOpen;
    private boolean orderable;
    private PointDto location;
}
