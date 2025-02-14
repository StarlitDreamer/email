package com.java.email.common.Redis;

import com.java.email.utils.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {
    private static final LogUtil logUtil = LogUtil.getLogger(RedisService.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    RedisService(RedisTemplate<String, Object> redisTemplate) {
        logUtil.info("RedisService.RedisService(),进入RedisService");
        this.redisTemplate = redisTemplate;
    }


    public String get(String key) {
        if (key == null) {
            return null;
        }
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return (String) value;
    }

    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            logUtil.error("redis set value error: " + e);
            return false;
        }
    }

    public boolean set(String key, Object value, Integer timeout, TimeUnit timeUnit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
            return true;
        } catch (Exception e) {
            logUtil.error("redis set value error: " + e);
            return false;
        }
    }

    public boolean delete(String key) {
        try {
            if (key != null && key.length() > 0) {
                redisTemplate.opsForValue().getAndDelete(key);
                return true;
            }
            return false;
        } catch (Exception e) {
            logUtil.error("redis delete key error: " + e);
            return false;
        }

    }
}
