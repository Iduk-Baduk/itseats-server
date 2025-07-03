package com.idukbaduk.itseats.memberaddress.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AddressListResponse {
    private Long addressId;
    private String mainAddress;
    private String detailAddress;
    private String addressCategory;
}
