package com.idukbaduk.itseats.store.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StoreDashboardResponse {
    private String storeName;
    private double customerRating;
    private String avgCookTime;
    private String cookTimeAccuracy;
    private String pickupTime;
    private String orderAcceptanceRate;
}
