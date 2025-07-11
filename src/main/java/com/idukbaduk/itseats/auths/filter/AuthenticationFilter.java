package com.idukbaduk.itseats.auths.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.idukbaduk.itseats.auths.dto.CustomMemberDetails;
import com.idukbaduk.itseats.external.jwt.dto.JwtTokenPair;
import com.idukbaduk.itseats.auths.dto.request.LoginRequest;
import com.idukbaduk.itseats.auths.error.AuthException;
import com.idukbaduk.itseats.auths.error.enums.AuthErrorCode;
import com.idukbaduk.itseats.auths.usecase.AuthUseCase;
import com.idukbaduk.itseats.global.util.CookieUtil;
import com.idukbaduk.itseats.member.entity.Member;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Slf4j
public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthUseCase authUseCase;

    public AuthenticationFilter(
            AuthenticationManager authenticationManager, AuthUseCase authUseCase
    ) {
        super(authenticationManager);
        this.authUseCase = authUseCase;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthException {
        try {
            LoginRequest credential = new ObjectMapper().readValue(request.getInputStream(), LoginRequest.class);

            if (Objects.isNull(credential.username()) || credential.username().isBlank()) {
                throw new AuthException(AuthErrorCode.AUTHORIZATION_USERNAME_EMPTY);
            }
            if (Objects.isNull(credential.password()) || credential.password().isBlank()) {
                throw new AuthException(AuthErrorCode.AUTHORIZATION_PASSWORD_EMPTY);
            }

            return getAuthenticationManager().authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credential.username(), credential.password(), new ArrayList<>())
            );
        } catch (IOException e) {
            throw new AuthException(AuthErrorCode.AUTHORIZATION_PARSE_FAILED);
        }
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult
    ) {
        Member member = ((CustomMemberDetails) authResult.getPrincipal()).member();
        JwtTokenPair jwtTokenPair = authUseCase.login(member.getMemberId());

        response.addHeader(
                jwtTokenPair.accessToken().type().getValue(),
                jwtTokenPair.accessToken().value()
        );

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                CookieUtil.create(
                        jwtTokenPair.refreshToken().type().name(),
                        jwtTokenPair.refreshToken().value(),
                        jwtTokenPair.refreshToken().duration(),
                        true,
                        true,
                        "None"
                ).toString()
        );
    }

}
