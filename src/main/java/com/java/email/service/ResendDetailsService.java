package com.java.email.service;

import com.java.email.entity.ResendDetails;
import com.java.email.repository.ResendDetailsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class ResendDetailsService {

    @Autowired
    private ResendDetailsRepository resendDetailsRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public ResendDetails createResendDetails(String emailTaskId) {
        // 拼接email_resend_id
        String emailResendId = "resend_" + emailTaskId;

        // 设置重发开始时间为当前时间一小时后
        Long startTime = Instant.now().plus(1, ChronoUnit.HOURS).getEpochSecond();

        // 创建 ResendDetails 对象
        ResendDetails resendDetails = new ResendDetails();
        resendDetails.setEmailResendId(emailResendId);
        resendDetails.setEmailTaskId(emailTaskId);
        resendDetails.setStatus(0);  // 设置状态为未重发
        resendDetails.setStartTime(startTime);

        // 将对象保存到 Elasticsearch
        resendDetailsRepository.save(resendDetails);

        // 将email_resend_id保存到Redis，设置过期时间24小时
        redisTemplate.opsForValue().set(emailResendId, emailTaskId, 24, TimeUnit.HOURS);

        return resendDetails;
    }
}
