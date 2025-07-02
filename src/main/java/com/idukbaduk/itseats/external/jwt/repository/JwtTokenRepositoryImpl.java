package com.idukbaduk.itseats.external.jwt.repository;

import com.idukbaduk.itseats.external.jwt.config.JwtTokenProperties;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
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
    public void save(String key, String value) {
        redisTemplate.opsForValue().set(
                String.format(REFRESH_TOKEN_KEY_PREFIX, key), value
        );
    }

    @Override
    public void saveWithTTL(String key, String value, Duration duration) {
        redisTemplate.opsForValue().set(String.format(REFRESH_TOKEN_KEY_PREFIX, key), value, duration);
    }

    @Override
    public Optional<String> findByKey(String key) {
        return Optional.ofNullable((String) redisTemplate.opsForValue().get(String.format(REFRESH_TOKEN_KEY_PREFIX, key)));
    }

    @Override
    public Boolean deleteByKey(String key) {
        return redisTemplate.delete(String.format(REFRESH_TOKEN_KEY_PREFIX, key));
    }

}
