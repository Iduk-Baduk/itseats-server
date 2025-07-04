package com.idukbaduk.itseats.memberaddress.controller;

import com.idukbaduk.itseats.global.response.BaseResponse;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.enums.AddressResponse;
import com.idukbaduk.itseats.memberaddress.service.MemberAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
                AddressResponse.CREATE_ADDRESS_SUCCESS,
                memberAddressService.createAddress(userDetails.getUsername(), addressCreateRequest)
        );
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAddressList(@AuthenticationPrincipal UserDetails userDetails) {
        return BaseResponse.toResponseEntity(
                AddressResponse.GET_ADDRESS_LIST_SUCCESS,
                memberAddressService.getAddressList(userDetails.getUsername())
        );
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<BaseResponse> updateAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressCreateRequest addressUpdateRequest,
            @PathVariable Long addressId) {
        return BaseResponse.toResponseEntity(
                AddressResponse.UPDATE_ADDRESS_SUCCESS,
                memberAddressService.updateAddress(userDetails.getUsername(), addressUpdateRequest, addressId)
        );
    }

    @DeleteMapping("/{addressId}")
    public ResponseEntity<BaseResponse> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        memberAddressService.deleteAddress(userDetails.getUsername(), addressId);
        return BaseResponse.toResponseEntity(
                AddressResponse.DELETE_ADDRESS_SUCCESS,
                addressId);
    }
}
