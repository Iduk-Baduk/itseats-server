package com.idukbaduk.itseats.external.redis.repository;

import java.util.Optional;

public interface RedisKeyValueRepository<K, T> {

    void save(K key, T value);
    Optional<T> findByKey(K key);
    Boolean deleteByKey(K key);

}
