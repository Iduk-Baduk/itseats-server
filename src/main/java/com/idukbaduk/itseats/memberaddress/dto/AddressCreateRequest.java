package com.idukbaduk.itseats.memberaddress.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressCreateRequest {

    private String mainAddress;
    private String detailAddress;
    private double locationX;
    private double locationY;
    private String addressCategory;
}
