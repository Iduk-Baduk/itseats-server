package com.idukbaduk.itseats.global.config;

import com.idukbaduk.itseats.auths.filter.AuthenticationFilter;
import com.idukbaduk.itseats.external.jwt.service.JwtTokenService;
import com.idukbaduk.itseats.member.service.MemberService;
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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] PERMIT_URL_ARRAY = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/api-docs/**",
            "/login"
    };

    private final JwtTokenService jwtTokenService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(PERMIT_URL_ARRAY).permitAll()
                        .anyRequest().permitAll()
                )

                .addFilter(getAuthenticationFilter(authenticationManager));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) {
        return new AuthenticationFilter(authenticationManager, jwtTokenService);
    }

}
