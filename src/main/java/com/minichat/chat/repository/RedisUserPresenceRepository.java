package com.minichat.chat.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
public class RedisUserPresenceRepository implements UserPresenceRepository {
    private static final String KEY_PREFIX = "online:user:";
    private static final String ONLINE_VALUE = "1";

    private final StringRedisTemplate stringRedisTemplate;

    public RedisUserPresenceRepository(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void setOnline(String username, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key(username), ONLINE_VALUE, ttl);
    }

    @Override
    public void refreshTtl(String username, Duration ttl) {
        stringRedisTemplate.expire(key(username), ttl);
    }

    @Override
    public void deleteOnline(String username) {
        stringRedisTemplate.delete(key(username));
    }

    @Override
    public boolean isOnline(String username) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key(username)));
    }

    private String key(String username) {
        return KEY_PREFIX + username;
    }
}

