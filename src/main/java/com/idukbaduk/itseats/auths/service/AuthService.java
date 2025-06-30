package com.idukbaduk.itseats.auths.service;

import com.idukbaduk.itseats.external.jwt.service.JwtTokenService;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtTokenService tokenService;
    private final MemberRepository memberRepository;

}
