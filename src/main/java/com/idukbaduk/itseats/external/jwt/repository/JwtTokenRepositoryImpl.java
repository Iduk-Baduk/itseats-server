package com.idukbaduk.itseats.external.jwt.repository;

import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class JwtTokenRepositoryImpl implements JwtTokenRepository {

    private static final String REFRESH_TOKEN_KEY_PREFIX = "member:%s:refresh_token:";

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveWithTTL(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(format(key), value, duration);
    }

    @Override
    public Optional<String> findByKey(String key) {
        return Optional.ofNullable((String) redisTemplate.opsForValue().get(format(key)));
    }

    @Override
    public Boolean deleteByKey(String key) {
        return redisTemplate.delete(format(key));
    }

    private String format(String key) {
        return String.format(REFRESH_TOKEN_KEY_PREFIX, key);
    }

}
