package com.idukbaduk.itseats.rider.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class NearByOrderRequest {
    private double latitude;
    private double longitude;

    public NearByOrderRequest(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}