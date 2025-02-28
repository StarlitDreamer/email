package com.java.email.controller;

import com.java.email.common.Result;
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
    @PostMapping("create/{currentUserId}")
    public Result createEmailTask(@PathVariable String currentUserId,@RequestBody CreateEmailTaskRequest request) {
        try {
            return Result.success(emailTaskService.createEmailTask(currentUserId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建循环邮件任务
     */
    @PostMapping("createCycle/{currentUserId}")
    public Result createCycleEmailTask(@PathVariable String currentUserId,@RequestBody CreateCycleEmailTaskRequest request) {
        try {
            return Result.success(emailTaskService.createCycleEmailTask(currentUserId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建节日邮件任务
     */
    @PostMapping("createFestival/{currentUserId}")
    public Result createFestivalEmailTask(@PathVariable String currentUserId,@RequestBody EmailTask request) {
        try {
            return Result.success(emailTaskService.createFestivalEmailTask(currentUserId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 改变生日任务状态
     */
    @PutMapping("updateBirth")
    public Result updateBirthEmailTask(@RequestBody UpdateBirthEmailTask request) {
        try {
            return Result.success(emailTaskService.updateBirthEmailTask(request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}