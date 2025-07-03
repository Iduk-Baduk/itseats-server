package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateResponse;
import com.idukbaduk.itseats.memberaddress.dto.AddressListResponse;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.entity.enums.AddressCategory;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberAddressService {

    private final MemberAddressRepository memberAddressRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public AddressCreateResponse createAddress(String username, AddressCreateRequest addressCreateRequest) {
        Member member = getMember(username);

        MemberAddress memberAddress = MemberAddress.builder()
                .member(member)
                .mainAddress(addressCreateRequest.getMainAddress())
                .detailAddress(addressCreateRequest.getDetailAddress())
                .location(GeoUtil.toPoint(addressCreateRequest.getLng(), addressCreateRequest.getLat()))
                .addressCategory(AddressCategory.valueOf(addressCreateRequest.getAddressCategory()))
                .build();
        memberAddressRepository.save(memberAddress);

        return AddressCreateResponse.builder()
                .addressId(memberAddress.getAddressId())
                .mainAddress(memberAddress.getMainAddress())
                .detailAddress(memberAddress.getDetailAddress())
                .addressCategory(memberAddress.getAddressCategory().name())
                .build();
    }

    @Transactional
    public AddressCreateResponse updateAddress(String username, AddressCreateRequest addressUpdateRequest, Long addressId) {
        Member member = getMember(username);

        MemberAddress memberAddress = getMemberAddress(member, addressId);

        memberAddress.updateAddress(
                addressUpdateRequest.getMainAddress(),
                addressUpdateRequest.getDetailAddress(),
                GeoUtil.toPoint(addressUpdateRequest.getLng(), addressUpdateRequest.getLat()),
                AddressCategory.valueOf(addressUpdateRequest.getAddressCategory())
        );

        return AddressCreateResponse.builder()
                .mainAddress(memberAddress.getMainAddress())
                .detailAddress(memberAddress.getDetailAddress())
                .addressCategory(memberAddress.getAddressCategory().name())
                .build();
    }

    @Transactional
    public void deleteAddress(String username, Long addressId) {
        Member member = getMember(username);
        MemberAddress memberAddress = getMemberAddress(member, addressId);

        memberAddressRepository.delete(memberAddress);
    }

    @Transactional(readOnly = true)
    public List<AddressListResponse> getAddressList(String username) {
        Member member = getMember(username);
        List<MemberAddress> memberAddresses = memberAddressRepository.findAllByMember(member);

        return memberAddresses.stream()
                .map(address -> AddressListResponse.builder()
                        .addressId(address.getAddressId())
                        .mainAddress(address.getMainAddress())
                        .detailAddress(address.getDetailAddress())
                        .addressCategory(address.getAddressCategory().name())
                        .build())
                .collect(Collectors.toList());
    }

    MemberAddress getMemberAddress(Member member, Long addressId) {
        return memberAddressRepository.findByMemberAndAddressId(member, addressId)
                .orElseThrow(() -> new MemberAddressException(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND));
    }

    private Member getMember(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
