package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.service.MemberService;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateResponse;
import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.entity.enums.AddressCategory;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.geo.Point;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAddressServiceTest {

    @Mock
    private MemberAddressRepository memberAddressRepository;

    @Mock
    private MemberService memberService;

    @InjectMocks
    private MemberAddressService memberAddressService;

    @Test
    @DisplayName("주소 추가 성공")
    void createAddress_success() {
        // given
        String mainAddress = "서울시 구름구 구름로100번길 10";
        String detailsAddress = "100호";
        double locationX = 126.9780;
        double locationY = 37.5665;
        String addressCategory = AddressCategory.HOUSE.name();

        Member mockMember = Member.builder().username("user01").build();
        AddressCreateRequest request = AddressCreateRequest.builder()
                .mainAddress(mainAddress)
                .detailAddress(detailsAddress)
                .locationX(locationX)
                .locationY(locationY)
                .addressCategory(addressCategory)
                .build();

        when(memberService.getMemberByUsername(any())).thenReturn(mockMember);
        when(memberAddressRepository.save(any(MemberAddress.class))).thenAnswer(i -> i.getArgument(0));

        ArgumentCaptor<MemberAddress> captor = ArgumentCaptor.forClass(MemberAddress.class);

        // when
        AddressCreateResponse response = memberAddressService.createAddress(mockMember.getUsername(), request);

        // then - response
        assertThat(response).isNotNull();
        assertThat(response.getAddressCategory()).isEqualTo(addressCategory);
        assertThat(response.getMainAddress()).isEqualTo(mainAddress);
        assertThat(response.getDetailAddress()).isEqualTo(detailsAddress);

        // then - save
        verify(memberService).getMemberByUsername(mockMember.getUsername());
        verify(memberAddressRepository).save(captor.capture());
        MemberAddress savedAddress = captor.getValue();

        assertThat(mockMember).isEqualTo(savedAddress.getMember());
        assertThat(mainAddress).isEqualTo(savedAddress.getMainAddress());
        assertThat(detailsAddress).isEqualTo(savedAddress.getDetailAddress());
        assertThat(addressCategory).isEqualTo(savedAddress.getAddressCategory().name());

        Point point = savedAddress.getLocation();
        assertThat(point.getX()).isEqualTo(locationX, within(0.001));
        assertThat(point.getY()).isEqualTo(locationY, within(0.001));
    }

    @Test
    @DisplayName("회원 주소 정보를 성공적으로 반환")
    void getMemberAddress_success() {
        // given
        Long memberAddressId = 1L;
        Member mockMember = Member.builder()
                .memberId(1L)
                .build();
        MemberAddress memberAddress = MemberAddress.builder()
                .addressId(memberAddressId)
                .member(mockMember)
                .build();

        when(memberAddressRepository.findByMemberAndAddressId(mockMember, memberAddressId))
                .thenReturn(Optional.of(memberAddress));

        // when
        MemberAddress result = memberAddressService.getMemberAddress(mockMember, memberAddressId);

        // then
        assertThat(result.getAddressId()).isEqualTo(memberAddress.getAddressId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 주소 조회시 예외 발생")
    void getMemberAddress_notExist() {
        // given
        Long memberAddressId = 1L;
        Member mockMember = Member.builder()
                .memberId(1L)
                .build();
        when(memberAddressRepository.findByMemberAndAddressId(mockMember, memberAddressId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberAddressService.getMemberAddress(mockMember, memberAddressId))
                .isInstanceOf(MemberAddressException.class)
                .hasMessageContaining(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND.getMessage());
    }
}
