package com.idukbaduk.itseats.external.jwt.filter;

import com.idukbaduk.itseats.auths.dto.CustomMemberDetails;
import com.idukbaduk.itseats.external.jwt.service.JwtTokenParser;
import com.idukbaduk.itseats.member.entity.Member;
import com.idukbaduk.itseats.member.repository.MemberRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
 * 5. 인증 실패 시 403 상태코드 반환
 * 
 * @author 개발팀
 * @since 2025-07-09
 */
@Slf4j
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
     * 5. 인증 실패 시 403 상태코드 반환
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
            if (token != null) { // Changed from StringUtils.hasText(token) to token != null
                log.debug("JWT 토큰이 발견되었습니다.");
                
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
                    // 사용자를 찾을 수 없는 경우 403 반환
                    sendForbiddenResponse(response, "유효하지 않은 사용자입니다.");
                    return;
                }
            } else {
                log.debug("Authorization 헤더에 JWT 토큰이 없습니다.");
            }
        } catch (ExpiredJwtException e) {
            log.warn("JWT 토큰이 만료되었습니다: {}", e.getMessage());
            sendForbiddenResponse(response, "토큰이 만료되었습니다.");
            return;
        } catch (UnsupportedJwtException e) {
            log.warn("지원하지 않는 JWT 토큰입니다: {}", e.getMessage());
            sendForbiddenResponse(response, "지원하지 않는 토큰 형식입니다.");
            return;
        } catch (MalformedJwtException e) {
            log.warn("잘못된 JWT 토큰 형식입니다: {}", e.getMessage());
            sendForbiddenResponse(response, "잘못된 토큰 형식입니다.");
            return;
        } catch (SignatureException e) {
            log.warn("JWT 토큰 서명이 유효하지 않습니다: {}", e.getMessage());
            sendForbiddenResponse(response, "토큰 서명이 유효하지 않습니다.");
            return;
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 비어있습니다: {}", e.getMessage());
            sendForbiddenResponse(response, "토큰이 비어있습니다.");
            return;
        } catch (Exception e) {
            log.warn("JWT 토큰 처리 중 예상치 못한 오류 발생: {}", e.getMessage());
            sendForbiddenResponse(response, "토큰 처리 중 오류가 발생했습니다.");
            return;
        }
        
        // 6. 필터 체인 계속 진행
        filterChain.doFilter(request, response);
    }

    /**
     * 403 Forbidden 응답을 전송합니다.
     * 
     * @param response HTTP 응답 객체
     * @param message 오류 메시지
     * @throws IOException IO 예외
     */
    private void sendForbiddenResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        // 애플리케이션의 BaseResponse 형식과 일치하는 JSON 응답
        String jsonResponse = String.format("""
            {
              "httpStatus": 403,
              "message": "%s",
              "data": null
            }
            """, message);
        
        response.getWriter().write(jsonResponse);
    }
} 
