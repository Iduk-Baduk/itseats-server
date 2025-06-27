package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.dto.CreateCustomerDto;
import com.idukbaduk.itseats.member.dto.response.CustomerCreateResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.entity.enums.MemberType;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.member.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    public Member getMemberByUsername(String username) {
        return memberRepository.findByUsername(username)
                .orElseThrow(() -> new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional
    public CustomerCreateResponse createCustomer(CreateCustomerDto createCustomerDto) {
        Member newMember = memberRepository.save(createCustomerDto.toEntity());
        return CustomerCreateResponse.of(newMember.getMemberId(), Boolean.TRUE);
    }

}
