package com.idukbaduk.itseats.auths.service;

import com.idukbaduk.itseats.auths.dto.CustomMemberDetails;
import com.idukbaduk.itseats.member.error.MemberException;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomMemberDetailService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public CustomMemberDetails loadUserByUsername(String username) throws MemberException {
        return new CustomMemberDetails(
                memberRepository.getOrThrowByUsername(username)
        );
    }

}
