package com.idukbaduk.itseats.global.config;

import com.idukbaduk.itseats.auths.filter.AuthenticationFilter;
import com.idukbaduk.itseats.auths.usecase.AuthUseCase;
import com.idukbaduk.itseats.external.jwt.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 설정 클래스
 * 
 * 주요 설정:
 * 1. JWT 인증 필터를 Spring Security 필터 체인에 추가
 * 2. 로그인 필터 설정 (/api/login)
 * 3. CORS 및 CSRF 설정
 * 4. URL별 권한 설정
 * 
 * @author 개발팀
 * @since 2025-07-09
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * 인증 없이 접근 가능한 URL 패턴들
     */
    private static final String[] PERMIT_URL_ARRAY = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/login"
    };

    private final AuthUseCase authUseCase;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Spring Security 필터 체인 설정
     * 
     * 필터 순서:
     * 1. JWT 인증 필터 (모든 요청에 대해 JWT 토큰 검증)
     * 2. 로그인 필터 (/api/login 요청 처리)
     * 
     * @param http HttpSecurity 객체
     * @param authenticationManager 인증 매니저
     * @return SecurityFilterChain
     * @throws Exception 설정 오류
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                // CSRF 보호 비활성화 (JWT 기반 인증이므로)
                .csrf(AbstractHttpConfigurer::disable)
                // CORS 설정 활성화
                .cors(Customizer.withDefaults())

                // URL별 권한 설정
                .authorizeHttpRequests(authorize -> authorize
                        // 인증 없이 접근 가능한 URL들
                        .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                        // 나머지 모든 요청은 허용 (JWT 필터에서 인증 처리)
                        .anyRequest().permitAll()
                )

                // 로그인 필터 추가 (POST /api/login 요청 처리)
                .addFilter(getAuthenticationFilter(authenticationManager))
                // JWT 인증 필터를 UsernamePasswordAuthenticationFilter 이전에 추가
                // 모든 요청에 대해 JWT 토큰을 먼저 검증하도록 함
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 비밀번호 인코더 설정 (BCrypt 사용)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 인증 매니저 설정
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 로그인 필터 생성
     * 
     * @param authenticationManager 인증 매니저
     * @return AuthenticationFilter
     */
    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        AuthenticationFilter filter = new AuthenticationFilter(authenticationManager, authUseCase);
        // 로그인 요청 URL 설정
        filter.setFilterProcessesUrl("/api/login");
        return filter;
    }
}
