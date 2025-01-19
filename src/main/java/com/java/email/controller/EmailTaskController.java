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

    /**
     * 更新邮件任务的操作状态
     *
     * @param emailTaskId   邮件任务ID
     * @param operateStatus 新的操作状态
     * @return 更新结果
     */
    @PutMapping("/reset-operate-status")
    public Result resetOperateStatus(
            @RequestParam String emailTaskId,  // 邮件任务ID
            @RequestParam int operateStatus) { // 新的操作状态
        try {
            // 调用服务层方法更新状态
            EmailTask updatedTask = emailTaskService.updateOperateStatus(emailTaskId, operateStatus);
            // 返回成功结果
            return Result.success(updatedTask);
        } catch (IllegalArgumentException e) {
            // 返回参数错误信息
            return Result.error(e.getMessage());
        } catch (Exception e) {
            // 返回系统错误信息
            return Result.error("更新任务操作状态失败");
        }
    }
}