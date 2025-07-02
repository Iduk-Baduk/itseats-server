package com.idukbaduk.itseats.external.jwt.config;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtTokenProperties(
        String secret,
        Expiration expiration
) {

    public record Expiration(long access, long refresh) { }

    public Duration accessTokenDuration() {
        return Duration.ofMillis(expiration.access());
    }

    public Duration refreshTokenDuration() {
        return Duration.ofMillis(expiration.refresh());
    }

}
