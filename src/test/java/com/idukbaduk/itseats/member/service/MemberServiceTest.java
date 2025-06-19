package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberService memberService;

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
}
