package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.entity.Email;
import com.java.email.model.request.ResetTaskStatusRequest;
import com.java.email.model.request.UpdateTaskStatusRequest;
import com.java.email.model.response.ResetTaskStatusResponse;
import com.java.email.model.response.UpdateTaskStatusResponse;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/emails")
public class EmailController {
    @Autowired
    private EmailService emailService;

    /**
     * 根据 emailTaskId 更新所有相关 email 的状态
     *
     * @return 更新后的邮件实体集合
     */
    @PutMapping("/update-status")
    public Result<UpdateTaskStatusResponse> updateEmailStatus(@RequestHeader String currentUserId, @RequestHeader int currentUserRole, @RequestBody UpdateTaskStatusRequest request) {
        try {
            // 调用服务层方法更新状态
            List<Email> updatedEmails = emailService.updateEmailStatusForAll(currentUserId,currentUserRole,request);

            // 创建响应列表
            List<UpdateTaskStatusResponse> responseList = updatedEmails.stream()
                    .map(email -> {
                        UpdateTaskStatusResponse response = new UpdateTaskStatusResponse();
                        response.setEmailTaskId(email.getEmailTaskId());
                        response.setEmailStatus(String.valueOf(email.getEmailStatus()));
                        return response;
                    })
                    .toList();
            UpdateTaskStatusResponse response = new UpdateTaskStatusResponse();
            response.setEmailTaskId(request.getEmailTaskId());
            response.setEmailStatus(request.getOperateStatus());
            return Result.success(response);  // 返回成功响应，包含更新后的邮件列表
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }

    /**
     * 根据 email_task_id 更新所有相关 email 的状态为 4
     *
     * @return 更新后的邮件实体集合
     */
    @PutMapping("/reset-status")
    public Result<ResetTaskStatusResponse> resetEmailStatus(@RequestBody ResetTaskStatusRequest request) {
        try {
            // 调用服务层方法根据 emailTaskId 更新状态为 4
            List<Email> updatedEmails = emailService.resetEmailStatusForAll(request);

            // 创建响应列表
            List<ResetTaskStatusResponse> responseList = updatedEmails.stream()
                    .map(email -> {
                        ResetTaskStatusResponse response = new ResetTaskStatusResponse();
                        response.setEmailTaskId(email.getEmailTaskId());
                        response.setEmailStatus(String.valueOf(email.getEmailStatus()));
                        return response;
                    })
                    .toList();

            ResetTaskStatusResponse response = new ResetTaskStatusResponse();
            response.setEmailTaskId(request.getTaskId());
            response.setEmailStatus(String.valueOf(4));
            return Result.success(response);  // 返回成功响应，包含更新后的邮件列表
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }
}