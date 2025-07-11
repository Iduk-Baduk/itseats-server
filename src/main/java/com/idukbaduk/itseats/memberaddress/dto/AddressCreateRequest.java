package com.idukbaduk.itseats.memberaddress.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressCreateRequest {

    private String mainAddress;
    private String detailAddress;
    private double lng;
    private double lat;
    private String addressCategory;
}
