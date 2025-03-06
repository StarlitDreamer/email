package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.request.CreateCycleEmailTaskRequest;
import com.java.email.model.request.CreateEmailTaskRequest;
import com.java.email.model.request.CreateFestivalEmailTaskRequest;
import com.java.email.model.request.UpdateBirthEmailTaskRequest;
import com.java.email.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-tasks")
public class EmailTaskController {

    @Autowired
    private EmailTaskService emailTaskService;

    // 根据 email_task_id 查询 email_type_id
    @GetMapping("/{emailTaskId}")
    public String getEmailTypeId(@PathVariable String emailTaskId) {
        String emailTypeId = emailTaskService.getEmailTypeId(emailTaskId);
        if (emailTypeId != null) {
            return emailTypeId;
        }
        return "未找到 emailTaskId 对应的 email_type_id";  // 或返回 404 错误
    }

    /**
     * 创建普通邮件任务
     */
    @PostMapping("create")
    public Result createEmailTask(@RequestHeader String currentUserId,@RequestBody CreateEmailTaskRequest request) {
        try {
            return Result.success(emailTaskService.createEmailTask(currentUserId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建循环邮件任务
     */
    @PostMapping("createCycle")
    public Result createCycleEmailTask(@RequestHeader String currentUserId,@RequestBody CreateCycleEmailTaskRequest request) {
        try {
            return Result.success(emailTaskService.createCycleEmailTask(currentUserId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 创建节日邮件任务
     */
    @PostMapping("createFestival")
    public Result createFestivalEmailTask(@RequestHeader String currentUserId,@RequestBody CreateFestivalEmailTaskRequest request) {
        try {
            return Result.success(emailTaskService.createFestivalEmailTask(currentUserId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 改变生日任务状态
     */
    @PutMapping("updateBirth/{tackId}")
    public Result updateBirthEmailTask(@RequestHeader String currentUserId,@PathVariable String tackId,@RequestBody UpdateBirthEmailTaskRequest request) {
        try {
            return Result.success(emailTaskService.updateBirthEmailTask(currentUserId,tackId,request));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}