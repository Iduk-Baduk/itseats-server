package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.global.util.GeoUtil;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateResponse;
import com.idukbaduk.itseats.memberaddress.dto.AddressListResponse;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.entity.enums.AddressCategory;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAddressServiceTest {

    @Mock
    private MemberAddressRepository memberAddressRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberAddressService memberAddressService;

    private final String username = "testuser";
    private Member member;
    private MemberAddress memberAddress;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .memberId(1L)
                .username(username)
                .build();

        memberAddress = MemberAddress.builder()
                .addressId(1L)
                .member(member)
                .build();
    }

    @Test
    @DisplayName("주소 추가 성공")
    void createAddress_success() {
        // given
        AddressCreateRequest request = AddressCreateRequest.builder()
                .mainAddress("서울시 구름구 구름로100번길 10")
                .detailAddress("100호")
                .lng(126.9780)
                .lat(37.5665)
                .addressCategory(AddressCategory.HOUSE.name())
                .build();

        when(memberRepository.findByUsername(any())).thenReturn(Optional.ofNullable(member));
        when(memberAddressRepository.save(any(MemberAddress.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<MemberAddress> captor = ArgumentCaptor.forClass(MemberAddress.class);

        // when
        AddressCreateResponse response = memberAddressService.createAddress(username, request);

        // then - response
        assertThat(response).isNotNull();
        assertThat(response.getAddressCategory()).isEqualTo(request.getAddressCategory());
        assertThat(response.getMainAddress()).isEqualTo(request.getMainAddress());
        assertThat(response.getDetailAddress()).isEqualTo(request.getDetailAddress());

        // then - save
        verify(memberRepository).findByUsername(username);
        verify(memberAddressRepository).save(captor.capture());
        MemberAddress savedAddress = captor.getValue();

        assertThat(member).isEqualTo(savedAddress.getMember());
        assertThat(request.getMainAddress()).isEqualTo(savedAddress.getMainAddress());
        assertThat(request.getDetailAddress()).isEqualTo(savedAddress.getDetailAddress());
        assertThat(request.getAddressCategory()).isEqualTo(savedAddress.getAddressCategory().name());

        Point point = savedAddress.getLocation();
        assertThat(point.getX()).isEqualTo(request.getLng(), within(0.001));
        assertThat(point.getY()).isEqualTo(request.getLat(), within(0.001));
    }

    @Test
    @DisplayName("회원 주소 정보를 성공적으로 반환")
    void getMemberAddress_success() {
        // given
        when(memberAddressRepository.findByMemberAndAddressId(member, 1L))
                .thenReturn(Optional.of(memberAddress));

        // when
        MemberAddress result = memberAddressService.getMemberAddress(member, 1L);

        // then
        assertThat(result.getAddressId()).isEqualTo(memberAddress.getAddressId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 주소 조회시 예외 발생")
    void getMemberAddress_notExist() {
        // given
        when(memberAddressRepository.findByMemberAndAddressId(member, 1L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberAddressService.getMemberAddress(member, 1L))
                .isInstanceOf(MemberAddressException.class)
                .hasMessageContaining(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("주소 수정 성공")
    void updateAddress_success() {
        // given
        Long addressId = 1L;

        // 기존 주소 객체 생성
        MemberAddress existingAddress = MemberAddress.builder()
                .addressId(addressId)
                .member(member)
                .mainAddress("서울시 구름구 구름로100번길 10")
                .detailAddress("100호")
                .location(GeoUtil.toPoint(126.9780, 37.5665))
                .addressCategory(AddressCategory.HOUSE)
                .build();

        // mock 설정
        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(memberAddressRepository.findByMemberAndAddressId(member, addressId))
                .thenReturn(Optional.of(existingAddress));

        // when
        AddressCreateRequest updateRequest = AddressCreateRequest.builder()
                .mainAddress("부산시 해운대구 우동 456")
                .detailAddress("202호")
                .lng(129.1604)
                .lat(35.163)
                .addressCategory(AddressCategory.COMPANY.name())
                .build();

        // 수정 요청 실행
        AddressCreateResponse response = memberAddressService.updateAddress(username, updateRequest, addressId);

        // then
        assertThat(response.getMainAddress()).isEqualTo(updateRequest.getMainAddress());
        assertThat(response.getDetailAddress()).isEqualTo(updateRequest.getDetailAddress());
        assertThat(response.getAddressCategory()).isEqualTo(updateRequest.getAddressCategory());


        assertThat(existingAddress.getMainAddress()).isEqualTo(updateRequest.getMainAddress());
        assertThat(existingAddress.getDetailAddress()).isEqualTo(updateRequest.getDetailAddress());
        assertThat(existingAddress.getAddressCategory().name()).isEqualTo(updateRequest.getAddressCategory());
    }

    @Test
    @DisplayName("주소 삭제 성공")
    void deleteAddress_success() {
        // given
        Long addressId = 1L;

        MemberAddress memberAddress = MemberAddress.builder()
                .addressId(addressId)
                .member(member)
                .mainAddress("서울시 구름구 구름로100번길 10")
                .detailAddress("100호")
                .location(GeoUtil.toPoint(126.9780, 37.5665))
                .addressCategory(AddressCategory.HOUSE)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(memberAddressRepository.findByMemberAndAddressId(member, addressId))
                .thenReturn(Optional.of(memberAddress));

        // when
        memberAddressService.deleteAddress(username, addressId);

        // then
        verify(memberAddressRepository).delete(memberAddress);
    }

    @Test
    @DisplayName("주소 목록 조회 성공")
    void getAddressList_success() {
        // given
        MemberAddress memberAddress1 = MemberAddress.builder()
                .addressId(1L)
                .member(member)
                .mainAddress("서울시 구름구 구름로100번길 10")
                .addressCategory(AddressCategory.HOUSE)
                .build();

        MemberAddress memberAddress2 = MemberAddress.builder()
                .addressId(2L)
                .member(member)
                .mainAddress("부산시 해운대구 우동 456")
                .addressCategory(AddressCategory.COMPANY)
                .build();

        List<MemberAddress> addressList = List.of(memberAddress1, memberAddress2);

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));
        when(memberAddressRepository.findAllByMember(member)).thenReturn(addressList);


        // when
        List<AddressListResponse> response = memberAddressService.getAddressList(username);

        // then
        assertThat(response).hasSize(2);
        assertThat(response.get(0).getAddressId()).isEqualTo(memberAddress1.getAddressId());
        assertThat(response.get(1).getAddressId()).isEqualTo(memberAddress2.getAddressId());
    }

}
