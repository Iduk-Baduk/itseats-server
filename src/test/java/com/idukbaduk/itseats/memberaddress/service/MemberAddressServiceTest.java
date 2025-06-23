package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateRequest;
import com.idukbaduk.itseats.memberaddress.dto.AddressCreateResponse;
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
                .locationX(126.9780)
                .locationY(37.5665)
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
        assertThat(point.getX()).isEqualTo(request.getLocationX(), within(0.001));
        assertThat(point.getY()).isEqualTo(request.getLocationY(), within(0.001));
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
}
