package com.idukbaduk.itseats.member.service;

import com.idukbaduk.itseats.member.dto.CustomerDto;
import com.idukbaduk.itseats.member.dto.response.CustomerCreateResponse;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.error.enums.MemberErrorCode;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import com.idukbaduk.itseats.member.util.PasswordUtil;
import java.util.Objects;
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
    public CustomerCreateResponse createCustomer(CustomerDto customerDto) {

        if (memberRepository.existsByUsername(customerDto.getUsername())) {
            throw new MemberException(MemberErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberRepository.existsByNickname(customerDto.getNickname())) {
            throw new MemberException(MemberErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (memberRepository.existsByEmail(customerDto.getEmail())) {
            throw new MemberException(MemberErrorCode.MEMBER_EMAIL_DUPLICATED);
        }

        String encryptedPassword = PasswordUtil.encrypt(customerDto.getPassword());
        Member newCustomer = memberRepository.save(customerDto.toEntity(encryptedPassword));
        return CustomerCreateResponse.of(newCustomer.getMemberId(), Objects.nonNull(newCustomer.getMemberId()));
    }

}
