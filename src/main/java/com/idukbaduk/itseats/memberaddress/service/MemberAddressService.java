package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateResponse;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.entity.enums.AddressCategory;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository memberAddressRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AddressCreateResponse createAddress(String username, AddressCreateRequest addressCreateRequest) {
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));

        MemberAddress memberAddress = MemberAddress.builder()
                .member(member)
                .mainAddress(addressCreateRequest.getMainAddress())
                .detailAddress(addressCreateRequest.getDetailAddress())
                .location(new Point(addressCreateRequest.getLocationX(), addressCreateRequest.getLocationY()))
                .addressCategory(AddressCategory.valueOf(addressCreateRequest.getAddressCategory()))
                .build();
        memberAddressRepository.save(memberAddress);

        return AddressCreateResponse.builder()
                .mainAddress(memberAddress.getMainAddress())
                .detailAddress(memberAddress.getDetailAddress())
                .addressCategory(memberAddress.getAddressCategory().name())
                .build();
    }

    public MemberAddress getMemberAddress(Member member, Long addressId) {
        return memberAddressRepository.findByMemberAndAddressId(member, addressId)
                .orElseThrow(() -> new MemberAddressException(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND));
    }
}
