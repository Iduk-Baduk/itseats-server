package com.idukbaduk.itseats.external.jwt.service;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

import com.idukbaduk.itseats.external.jwt.entity.enums.JwtTokenType;
import com.idukbaduk.itseats.external.jwt.error.JwtTokenException;
import com.idukbaduk.itseats.external.jwt.config.JwtTokenProperties;
import com.idukbaduk.itseats.external.jwt.error.enums.JwtTokenErrorCode;
import com.idukbaduk.itseats.global.util.ClockUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Header;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenService {

    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtTokenProperties jwtTokenProperties;
    private final JwtTokenRepository redisTokenService;

    public String issueToken(String memberId, JwtTokenType type) {
        if (Objects.nonNull(type)) {
            switch (type) {
                case ACCESS_TOKEN -> {
                    return createToken(memberId, jwtTokenProperties.expiration().access(), type);
                }
                case REFRESH_TOKEN -> {
                    String refreshToken = createToken(memberId, jwtTokenProperties.expiration().refresh(), type);
                    redisTokenService.save(memberId, refreshToken);
                    return refreshToken;
                }
            }
        }
        throw new JwtTokenException(JwtTokenErrorCode.TOKEN_ISSUE_FAIL);
    }

    public String reissueAccessToken(String memberId, String token) {
        Optional<String> savedRefreshToken = redisTokenService.findByKey(memberId);

        if (savedRefreshToken.isEmpty()) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_EXPIRED);
        }

        if (!MessageDigest.isEqual(
                savedRefreshToken.get().getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))
        ) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_UNTRUSTWORTHY);
        }

        return createToken(memberId, jwtTokenProperties.expiration().access(), JwtTokenType.ACCESS_TOKEN);
    }

    private String createToken(String memberId, long expirationTime, JwtTokenType type) {
        LocalDateTime now = ClockUtil.getLocalDateTime();
        return TOKEN_PREFIX.concat(
                Jwts.builder()
                        .header()
                        .add("type", type.getValue())
                        .and()
                        .subject(memberId)
                        .issuedAt(ClockUtil.convertToDate(now))
                        .expiration(ClockUtil.getExpirationDate(now, expirationTime))
                        .signWith(
                                Keys.hmacShaKeyFor(jwtTokenProperties.secret().getBytes(StandardCharsets.UTF_8)))
                        .compact()
        );
    }

    public Boolean discardRefreshToken(String memberId) {
        return redisTokenService.deleteByKey(memberId);
    }
    private JwtParser getParser() {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtTokenProperties.secret().getBytes(StandardCharsets.UTF_8)))
                .build();
    }

    public Header parseHeader(String token) {
        return getParser()
                .parseSignedClaims(removePrefix(token))
                .getHeader();
    }

    public Claims parseClaims(String token) {
        return getParser()
                .parseSignedClaims(removePrefix(token))
                .getPayload();
    }

    public String resolveToken(HttpServletRequest request) {
        return request.getHeader(AUTHORIZATION);
    }

    private String removePrefix(String token) {
        if (!token.startsWith(TOKEN_PREFIX)) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_UNTRUSTWORTHY);
        }
        return token.replace(TOKEN_PREFIX, "");
    }

    public boolean isValid(String token) throws JwtException {
        if (Objects.isNull(token)) {
            return false;
        }

        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_UNTRUSTWORTHY);
        }
    }

}