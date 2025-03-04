package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.entity.Email;
import com.java.email.model.request.ResendEmailRequest;
import com.java.email.model.request.ResetTaskStatusRequest;
import com.java.email.model.request.UpdateTaskStatusRequest;
import com.java.email.model.response.ResetTaskStatusResponse;
import com.java.email.model.response.UpdateTaskStatusResponse;
import com.java.email.repository.EmailPausedRepository;
import com.java.email.service.EmailService;
import com.java.email.service.ResendDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
public class EmailController {
    @Autowired
    private EmailService emailService;

    @Autowired
    private ResendDetailsService resendDetailsService;

    @Autowired
    private EmailPausedRepository emailPausedRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostMapping("/resend")
    public Result<String> resendEmail(@RequestBody ResendEmailRequest request) {
        // 拼接 "resend_" 字符串
        String emailResendId = "resend_" + request.getEmailId();

        // 将拼接后的 emailResendId 存入 Redis
        resendDetailsService.saveEmailResendId(emailResendId);

        // 返回响应，包含拼接后的 emailResendId
        return Result.success(emailResendId);
    }

//    @PostMapping("/pause")
//    public Result pauseEmailTask(@RequestBody EmailPausedRequest request) {
//        EmailPaused emailPaused = new EmailPaused();
//        emailPaused.setEmailTaskId(request.getEmailTaskId());
//
//        emailPausedRepository.save(emailPaused);
//        return Result.success("Email task paused successfully");
//    }
//
//    @PostMapping("/begin")
//    public Result beginEmailTask(@RequestBody EmailBeginRequest request) {
//        String emailTaskId = request.getEmailTaskId();
//        Optional<EmailPaused> emailPausedOptional = emailPausedRepository.findById(emailTaskId);
//
//        if (emailPausedOptional.isEmpty()) {
//            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "没有暂停该任务");
//        }
//
//        EmailPaused emailPaused = emailPausedOptional.get();
//        emailPausedRepository.delete(emailPaused);
//        redisTemplate.opsForValue().set("email_paused:" + emailTaskId, emailTaskId);
//
//        return Result.success("Email task resumed and stored in Redis");
//    }

    /**
     * 根据 emailTaskId 更新所有相关 email 的状态
     *
     * @return 更新后的邮件实体集合
     */
    @PutMapping("/update-status")
    public Result<UpdateTaskStatusResponse> updateEmailStatus(@RequestHeader String currentUserId, @RequestHeader int currentUserRole, @RequestBody UpdateTaskStatusRequest request) {
        Integer operateStatus = request.getOperateStatus();

        if (operateStatus == 1) {
            emailService.beginEmailTask(request);
        } else if (operateStatus == 2) {
            emailService.pauseEmailTask(request);
        }

        try {
            // 调用服务层方法更新状态
            Email updatedEmail = emailService.updateEmailStatusForAll(currentUserId, currentUserRole, request);
//            // 创建响应列表
//            List<UpdateTaskStatusResponse> responseList = updatedEmails.stream()
//                    .map(email -> {
//                        UpdateTaskStatusResponse response = new UpdateTaskStatusResponse();
//                        response.setEmailTaskId(email.getEmailTaskId());
//                        response.setEmailStatus(email.getEmailStatus());
//                        return response;
//                    })
//                    .toList();
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
    public Result<ResetTaskStatusResponse> resetEmailStatus(@RequestHeader String currentUserId, @RequestHeader int currentUserRole, @RequestBody ResetTaskStatusRequest request) {
        try {
            // 调用服务层方法根据 emailTaskId 更新状态为 4
            Email updatedEmails = emailService.resetEmailStatusForAll(currentUserId, currentUserRole, request);

//            // 创建响应列表
//            List<ResetTaskStatusResponse> responseList = updatedEmails.stream()
//                    .map(email -> {
//                        ResetTaskStatusResponse response = new ResetTaskStatusResponse();
//                        response.setEmailTaskId(email.getEmailTaskId());
//                        response.setEmailStatus(String.valueOf(email.getEmailStatus()));
//                        return response;
//                    })
//                    .toList();

            ResetTaskStatusResponse response = new ResetTaskStatusResponse();
            response.setEmailTaskId(request.getTaskId());
            response.setEmailStatus(String.valueOf(4));
            return Result.success(response);  // 返回成功响应，包含更新后的邮件列表
        } catch (Exception e) {
            return Result.error("更新邮件状态失败: " + e.getMessage());  // 返回错误响应
        }
    }
}