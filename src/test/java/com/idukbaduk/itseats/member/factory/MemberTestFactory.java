package com.idukbaduk.itseats.member.factory;

import com.idukbaduk.itseats.member.dto.CustomerDto;
import com.idukbaduk.itseats.member.entity.Member;

public class MemberTestFactory {

    public static Member createFromDto(CustomerDto dto, Long id, String encodedPassword) {
        Member member = dto.toEntity(encodedPassword);
        member.setMemberId(id);
        return member;
    }

    public static CustomerDto validUser() {
        return CustomerDto.builder()
                .username("user1")
                .password("rawPass")
                .name("name")
                .nickname("nick1")
                .email("user1@email.com")
                .build();
    }

    public static CustomerDto withDuplicatedUsername() {
        return CustomerDto.builder()
                .username("duplicate")
                .password("anyPass")
                .name("이름")
                .nickname("uniqueNick")
                .email("unique@email.com")
                .build();
    }

    public static CustomerDto withDuplicatedNickname() {
        return CustomerDto.builder()
                .username("uniqueUser")
                .password("anyPass")
                .name("이름")
                .nickname("duplicate")
                .email("unique@email.com")
                .build();
    }

    public static CustomerDto withDuplicatedEmail() {
        return CustomerDto.builder()
                .username("uniqueUser")
                .password("anyPass")
                .name("이름")
                .nickname("uniqueNick")
                .email("duplicate@email.com")
                .build();
    }

}
