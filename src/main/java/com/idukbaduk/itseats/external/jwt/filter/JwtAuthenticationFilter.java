package com.idukbaduk.itseats.external.jwt.filter;

import com.idukbaduk.itseats.auths.dto.CustomMemberDetails;
import com.idukbaduk.itseats.external.jwt.service.JwtTokenParser;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 토큰 기반 인증 필터
 * 
 * 모든 HTTP 요청에 대해 JWT 토큰을 검증하고, 유효한 토큰이 있는 경우
 * Spring Security의 SecurityContextHolder에 인증 정보를 설정합니다.
 * 
 * 주요 기능:
 * 1. Authorization 헤더에서 JWT 토큰 추출
 * 2. 토큰 유효성 검증 및 Claims 파싱
 * 3. 토큰의 subject(memberId)로 사용자 정보 조회
 * 4. SecurityContextHolder에 인증 정보 설정
 * 
 * @author 개발팀
 * @since 2025-07-09
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenParser jwtTokenParser;
    private final MemberRepository memberRepository;

    /**
     * HTTP 요청을 필터링하여 JWT 토큰을 처리합니다.
     * 
     * 처리 과정:
     * 1. Authorization 헤더에서 JWT 토큰 추출
     * 2. 토큰이 존재하면 검증 및 파싱
     * 3. 토큰의 subject(memberId)로 사용자 조회
     * 4. 유효한 사용자인 경우 SecurityContextHolder에 인증 정보 설정
     * 5. 예외 발생 시 로그만 남기고 필터 체인 계속 진행
     * 
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외
     * @throws IOException IO 예외
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 1. Authorization 헤더에서 JWT 토큰 추출
            String token = jwtTokenParser.resolveToken(request);
            
            // 2. 토큰이 존재하고 유효한 경우에만 처리
            if (StringUtils.hasText(token)) {
                log.debug("JWT 토큰 발견: {}", token.substring(0, Math.min(20, token.length())) + "...");
                
                // 3. JWT 토큰 검증 및 Claims 파싱
                Claims claims = jwtTokenParser.parseClaims(token);
                Long memberId = Long.parseLong(claims.getSubject());
                
                log.debug("JWT 토큰에서 추출한 memberId: {}", memberId);
                
                // 4. memberId로 사용자 정보 조회
                Member member = memberRepository.findById(memberId)
                        .orElse(null);
                
                // 5. 유효한 사용자인 경우 SecurityContextHolder에 인증 정보 설정
                if (member != null) {
                    CustomMemberDetails userDetails = new CustomMemberDetails(member);
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("사용자 인증 정보 설정 완료: {}", member.getUsername());
                } else {
                    log.warn("JWT 토큰의 memberId({})에 해당하는 사용자를 찾을 수 없습니다.", memberId);
                }
            } else {
                log.debug("Authorization 헤더에 JWT 토큰이 없습니다.");
            }
        } catch (Exception e) {
            // 6. JWT 토큰 처리 중 오류 발생 시 로그만 남기고 필터 체인 계속 진행
            // (인증되지 않은 요청도 정상적으로 처리되도록 함)
            log.debug("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
        }
        
        // 7. 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }
} 
