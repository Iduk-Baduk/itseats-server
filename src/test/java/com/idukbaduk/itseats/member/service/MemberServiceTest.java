package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.dto.CustomerDto;
import com.idukbaduk.itseats.member.dto.response.CustomerCreateResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.factory.MemberTestFactory;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks
    private MemberService memberService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원 정보를 성공적으로 반환")
    void getMemberByUsername_success() {
        // given
        String username = "testUser";
        Member member = Member.builder()
                .username(username)
                .build();

        when(memberRepository.findByUsername(username)).thenReturn(Optional.of(member));

        // when
        Member result = memberService.getMemberByUsername(username);

        // then
        assertThat(result.getUsername()).isEqualTo(username);
    }

    @Test
    @DisplayName("존재하지 않는 회원 정보 조회시 예외 발생")
    void getMemberByUsername_notExist() {
        // given
        String username = "unknownUser";
        when(memberRepository.findByUsername(username)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> memberService.getMemberByUsername(username))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 가입을 성공적으로 진행")
    void createCustomer() {
        // given
        CustomerDto dto = MemberTestFactory.validUser();
        String encryptedPassword = "$2a$10$abc";
        Member expectedMember = MemberTestFactory.createFromDto(dto, 1L, encryptedPassword);

        given(memberRepository.existsByUsername(dto.getUsername())).willReturn(false);
        given(memberRepository.existsByNickname(dto.getNickname())).willReturn(false);
        given(memberRepository.existsByEmail(dto.getEmail())).willReturn(false);
        given(memberRepository.save(any())).willReturn(expectedMember);

        // when
        when(passwordEncoder.encode("rawPass")).thenReturn(encryptedPassword);
        CustomerCreateResponse response = memberService.createCustomer(dto);

        // then
        assertThat(expectedMember.getMemberId()).isEqualTo(response.memberId());

    }

    @Test
    @DisplayName("중복된 username일 경우 예외 발생")
    void throwException_whenUsernameDuplicated() {
        // given
        CustomerDto dto = MemberTestFactory.withDuplicatedUsername();
        given(memberRepository.existsByUsername(dto.getUsername())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createCustomer(dto))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_USERNAME_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("중복된 nickname일 경우 예외 발생")
    void throwException_whenNicknameDuplicated() {
        // given
        CustomerDto dto = MemberTestFactory.withDuplicatedNickname();
        given(memberRepository.existsByUsername(dto.getUsername())).willReturn(false);
        given(memberRepository.existsByNickname(dto.getNickname())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createCustomer(dto))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("중복된 email일 경우 예외 발생")
    void throwException_whenEmailDuplicated() {
        // given
        CustomerDto dto = MemberTestFactory.withDuplicatedEmail();
        given(memberRepository.existsByUsername(dto.getUsername())).willReturn(false);
        given(memberRepository.existsByNickname(dto.getNickname())).willReturn(false);
        given(memberRepository.existsByEmail(dto.getEmail())).willReturn(true);

        // when & then
        assertThatThrownBy(() -> memberService.createCustomer(dto))
                .isInstanceOf(MemberException.class)
                .hasMessageContaining(MemberErrorCode.MEMBER_EMAIL_DUPLICATED.getMessage());
    }

}
