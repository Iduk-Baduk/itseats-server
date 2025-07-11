package com.idukbaduk.itseats.external.jwt.repository;

import com.idukbaduk.itseats.external.redis.repository.RedisKeyValueRepository;

public interface JwtTokenRepository extends RedisKeyValueRepository<String, String> {

}
