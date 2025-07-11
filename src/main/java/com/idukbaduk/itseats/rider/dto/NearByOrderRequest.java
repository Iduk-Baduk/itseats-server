package com.idukbaduk.itseats.rider.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NearByOrderRequest {
    private Double latitude;
    private Double longitude;
}
