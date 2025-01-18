package com.java.email.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    // 设置键值对
    public void setKey(String key, String value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        redisTemplate.opsForValue().set(key, value);
    }

    // 获取键对应的值
    public String getKey(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 删除键
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}