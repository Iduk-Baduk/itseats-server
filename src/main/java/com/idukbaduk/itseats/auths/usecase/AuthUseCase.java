package com.idukbaduk.itseats.auths.usecase;

import com.idukbaduk.itseats.external.jwt.dto.JwtTokenPair;
import com.idukbaduk.itseats.external.jwt.entity.JwtToken;
import com.idukbaduk.itseats.external.jwt.error.JwtTokenException;
import com.idukbaduk.itseats.external.jwt.error.enums.JwtTokenErrorCode;
import com.idukbaduk.itseats.external.jwt.service.JwtTokenIssuer;
import com.idukbaduk.itseats.external.jwt.service.JwtTokenParser;
import com.idukbaduk.itseats.external.jwt.service.RedisJwtRefreshTokenStore;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwt;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthUseCase {

    private final JwtTokenIssuer jwtTokenIssuer;
    private final JwtTokenParser jwtTokenParser;
    private final RedisJwtRefreshTokenStore redisJwtRefreshTokenStore;

    public JwtTokenPair login(Long memberId) {
        JwtToken accessToken = jwtTokenIssuer.issueAccessToken(memberId);
        JwtToken refreshToken = jwtTokenIssuer.issueRefreshToken(memberId);
        redisJwtRefreshTokenStore.store(memberId, refreshToken.value(), refreshToken.duration());
        return JwtTokenPair.of(accessToken, refreshToken);
    }

    public JwtToken reissue(Long memberId, String tokenFromClient) {
        Optional<String> savedToken = redisJwtRefreshTokenStore.getTokenByMemberId(memberId);
        if (savedToken.isEmpty()) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_EXPIRED);
        }
        if (!isEqualToken(tokenFromClient, savedToken.get())) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_UNTRUSTWORTHY);
        }
        return jwtTokenIssuer.issueAccessToken(memberId);
    }

    private boolean isEqualToken(String token, String savedToken) {
        return MessageDigest.isEqual(
                token.getBytes(StandardCharsets.UTF_8),
                savedToken.getBytes(StandardCharsets.UTF_8)
        );
    }

    public Boolean logout(Long memberId) {
        return redisJwtRefreshTokenStore.invalidateTokenByMemberId(memberId);
    }

    public boolean verify(String token) throws JwtTokenException {
        if (Objects.isNull(token)) {
            return false;
        }
        try {
            jwtTokenParser.parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_EXPIRED);
        } catch (SignatureException | MalformedJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_UNTRUSTWORTHY);
        }
    }

}
