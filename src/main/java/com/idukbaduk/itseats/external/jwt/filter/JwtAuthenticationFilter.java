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

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenParser jwtTokenParser;
    private final MemberRepository memberRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            String token = jwtTokenParser.resolveToken(request);
            
            if (StringUtils.hasText(token)) {
                Claims claims = jwtTokenParser.parseClaims(token);
                Long memberId = Long.parseLong(claims.getSubject());
                
                Member member = memberRepository.findById(memberId)
                        .orElse(null);
                
                if (member != null) {
                    CustomMemberDetails userDetails = new CustomMemberDetails(member);
                    UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            log.debug("JWT 토큰 처리 중 오류 발생: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
} 
