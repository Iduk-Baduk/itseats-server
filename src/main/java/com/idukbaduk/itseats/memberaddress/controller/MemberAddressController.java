package com.idukbaduk.itseats.memberaddress.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.enums.AddressResponse;
import com.idukbaduk.itseats.memberaddress.service.MemberAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/addresses")
@RequiredArgsConstructor
public class MemberAddressController {

    private final MemberAddressService memberAddressService;

    @PostMapping
    public ResponseEntity<BaseResponse> createAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressCreateRequest addressCreateRequest) {
        return BaseResponse.toResponseEntity(
                AddressResponse.ADDRESS_CREATE_SUCCESS,
                memberAddressService.createAddress(userDetails.getUsername(), addressCreateRequest)
        );
    }
}
