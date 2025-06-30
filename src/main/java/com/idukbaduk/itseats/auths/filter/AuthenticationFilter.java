package com.idukbaduk.itseats.auths.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.auths.dto.request.LoginRequest;
import com.idukbaduk.itseats.auths.service.AuthService;
import com.idukbaduk.itseats.external.jwt.service.JwtTokenService;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.service.MemberService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;

    public AuthenticationFilter(
            AuthenticationManager authenticationManager, MemberService memberService, JwtTokenService jwtTokenService
    ) {
        super(authenticationManager);
        this.memberService = memberService;
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            LoginRequest credential = objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credential.username(), credential.password(), new ArrayList<>()
                    ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult
    ) {
        // todo: 추후 토큰 발행 로직 구현 필요
    }

}
