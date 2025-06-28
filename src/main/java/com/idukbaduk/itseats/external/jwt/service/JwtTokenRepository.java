package com.idukbaduk.itseats.external.jwt.service;

import com.idukbaduk.itseats.external.redis.repository.RedisKeyValueRepository;

public interface JwtTokenRepository extends RedisKeyValueRepository<String, String> {

}
