package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.EmailTask;
import com.java.email.service.EmailTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-tasks")
public class EmailTaskController {

    @Autowired
    private EmailTaskService emailTaskService;

    /**
     * 根据邮件任务 ID 修改任务操作状态
     *
     * @param emailTaskId   邮件任务 ID
     * @param operateStatus 新的任务操作状态
     * @return 操作结果
     */
    @PutMapping("/operate-status")
    public Result updateOperateStatus(
            @RequestParam String emailTaskId,  // 改为 @RequestParam
            @RequestParam int operateStatus) {
        try {
            EmailTask updatedTask = emailTaskService.updateOperateStatus(emailTaskId, operateStatus);
            return Result.success(updatedTask);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新任务操作状态失败");
        }
    }
}