package com.idukbaduk.itseats.external.jwt.config;

import com.idukbaduk.itseats.external.jwt.error.JwtTokenException;
import com.idukbaduk.itseats.external.jwt.error.enums.JwtTokenErrorCode;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtTokenProperties(
        String secret,
        Expiration expiration
) {

    @PostConstruct
    public void validate() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new JwtTokenException(JwtTokenErrorCode.TOKEN_SECRET_KEY_INVALID_LENGTH);
        }
    }
    public record Expiration(long access, long refresh) { }

    public Duration accessTokenDuration() {
        return Duration.ofMillis(expiration.access());
    }

    public Duration refreshTokenDuration() {
        return Duration.ofMillis(expiration.refresh());
    }

}
