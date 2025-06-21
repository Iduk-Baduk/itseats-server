package com.idukbaduk.itseats.memberaddress.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressCreateResponse {

    private String mainAddress;
    private String detailAddress;
    private String addressCategory;
}
