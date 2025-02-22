package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.service.EmailDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/emailDetails")
public class EmailDetailController {

    @Autowired
    private EmailDetailService emailDetailService;

    // 获取所有状态码为 500 的邮件的 emailTaskId
    @GetMapping("/undelivered-emails")
    public Result<List<String>> getEmailTaskIdsForErrorCode500() {
        return Result.success(emailDetailService.getEmailTaskIdsForErrorCode500());
    }

    // 获取退信数量接口
    @GetMapping("/bounce-count")
    public long getBounceCount() {
        return emailDetailService.getBounceCount();
    }

    // 获取送达数量接口
    @GetMapping("/delivered-count")
    public long getDeliveredCount() {
        return emailDetailService.getDeliveredCount();
    }
}