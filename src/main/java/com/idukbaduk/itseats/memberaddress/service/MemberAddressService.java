package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository memberAddressRepository;

    public MemberAddress getMemberAddress(Long addressId) {
        return memberAddressRepository.findById(addressId)
                .orElseThrow(() -> new MemberAddressException(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND));
    }
}
