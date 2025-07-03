package com.idukbaduk.itseats.external.jwt.service;

import com.idukbaduk.itseats.external.jwt.repository.JwtTokenRepository;
import java.time.Duration;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisJwtRefreshTokenStore {

    private final JwtTokenRepository jwtTokenRepository;

    public void store(Long memberId, String refreshToken, Duration duration) {
        jwtTokenRepository.saveWithTTL(String.valueOf(memberId), refreshToken, duration);
    }

    public Optional<String> getTokenByMemberId(Long memberId) {
        return jwtTokenRepository.findByKey(String.valueOf(memberId));
    }

    public Boolean invalidateTokenByMemberId(Long memberId) {
        return jwtTokenRepository.deleteByKey(String.valueOf(memberId));
    }

}
