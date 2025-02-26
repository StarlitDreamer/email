package com.java.email.controller;

import com.java.email.model.entity.EmailTask;
import com.java.email.model.request.CreateCycleEmailTaskRequest;
import com.java.email.model.request.CreateEmailTaskRequest;
import com.java.email.model.request.UpdateBirthEmailTask;
import com.java.email.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-tasks")
public class EmailTaskController {

    @Autowired
    private EmailTaskService emailTaskService;

    /**
     * 创建普通邮件任务
     */
    @PostMapping("create")
    public String createEmailTask(@RequestBody CreateEmailTaskRequest request) {
        System.out.println(request.getSenderId());
        return emailTaskService.createEmailTask(request);
    }

    /**
     * 创建循环邮件任务
     */
    @PostMapping("createCycle")
    public String createCycleEmailTask(@RequestBody CreateCycleEmailTaskRequest request) {
        return emailTaskService.createCycleEmailTask(request);
    }

    /**
     * 创建节日邮件任务
     */
    @PostMapping("createFestival")
    public String createFestivalEmailTask(@RequestBody EmailTask request) {
        return emailTaskService.createFestivalEmailTask(request);
    }

    /**
     * 改变生日任务状态
     */
    @PutMapping("updateBirth")
    public String updateBirthEmailTask(@RequestBody UpdateBirthEmailTask request) {
        return emailTaskService.updateBirthEmailTask(request);
    }
}