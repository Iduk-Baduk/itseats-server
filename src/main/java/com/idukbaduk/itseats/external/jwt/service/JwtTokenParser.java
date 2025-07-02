package com.idukbaduk.itseats.external.jwt.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.idukbaduk.itseats.external.jwt.config.JwtTokenProperties;
import com.idukbaduk.itseats.external.jwt.error.JwtTokenException;
import com.idukbaduk.itseats.external.jwt.error.enums.JwtTokenErrorCode;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenParser {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProperties jwtTokenProperties;

    public Header parseHeader(String token) {
        return getJwtParser()
                .parseSignedClaims(removePrefix(token))
                .getHeader();
    }

    public Claims parseClaims(String token) {
        return getJwtParser()
                .parseSignedClaims(removePrefix(token))
                .getPayload();
    }

    private JwtParser getJwtParser() {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtTokenProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    private String removePrefix(String token) {
        if (!token.startsWith(TOKEN_PREFIX)) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_UNTRUSTWORTHY);
        }
        return token.substring(TOKEN_PREFIX.length());
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION);
    }

}
