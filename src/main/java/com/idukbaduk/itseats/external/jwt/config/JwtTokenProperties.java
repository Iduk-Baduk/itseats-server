package com.idukbaduk.itseats.external.jwt.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtTokenProperties(
        String secret,
        Expiration expiration
) {
    public record Expiration(long access, long refresh) { }
}

