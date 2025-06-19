package com.idukbaduk.itseats.memberaddress.service;

import com.idukbaduk.itseats.memberaddress.entity.MemberAddress;
import com.idukbaduk.itseats.memberaddress.error.MemberAddressException;
import com.idukbaduk.itseats.memberaddress.error.enums.MemberAddressErrorCode;
import com.idukbaduk.itseats.memberaddress.repository.MemberAddressRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberAddressServiceTest {

    @Mock
    private MemberAddressRepository memberAddressRepository;

    @InjectMocks
    private MemberAddressService memberAddressService;

    @Test
    @DisplayName("회원 주소 정보를 성공적으로 반환")
    void getMemberAddress_success() {
        // given
        Long memberAddressId = 1L;
        MemberAddress memberAddress = MemberAddress.builder()
                .addressId(memberAddressId)
                .build();

        when(memberAddressRepository.findById(memberAddressId)).thenReturn(Optional.of(memberAddress));

        // when
        MemberAddress result = memberAddressService.getMemberAddress(memberAddressId);

        // then
        assertThat(result.getAddressId()).isEqualTo(memberAddress.getAddressId());
    }

    @Test
    @DisplayName("존재하지 않는 회원 주소 조회시 예외 발생")
    void getMemberAddress_notExist() {
        // given
        Long memberAddressId = 1L;
        when(memberAddressRepository.findById(memberAddressId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberAddressService.getMemberAddress(memberAddressId))
                .isInstanceOf(MemberAddressException.class)
                .hasMessageContaining(MemberAddressErrorCode.MEMBER_ADDRESS_NOT_FOUND.getMessage());
    }
}