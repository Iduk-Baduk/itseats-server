package com.idukbaduk.itseats.order.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressInfoDTO {

    private double lat;
    private double lng;
}
