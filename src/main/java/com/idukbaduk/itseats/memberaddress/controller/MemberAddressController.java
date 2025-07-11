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

    /**
     * 사용자의 주소 목록 조회
     * 
     * 로그인한 사용자의 주소 목록을 반환합니다.
     * 로그인하지 않은 사용자의 경우 빈 목록을 반환합니다.
     * 
     * @param userDetails Spring Security에서 주입하는 사용자 정보
     *                    JWT 인증 필터에서 설정된 인증 정보 (null일 수 있음)
     * @return 주소 목록 (로그인하지 않은 경우 빈 목록)
     */
    @GetMapping
    public ResponseEntity<BaseResponse> getAddressList(@AuthenticationPrincipal UserDetails userDetails) {
        // JWT 인증 필터에서 설정된 인증 정보가 없는 경우 (로그인하지 않은 사용자)
        if (userDetails == null) {
            return BaseResponse.toResponseEntity(
                    AddressResponse.GET_ADDRESS_LIST_SUCCESS,
                    memberAddressService.getAddressList(null)
            );
        }
        // 로그인한 사용자의 경우 해당 사용자의 주소 목록 반환
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
