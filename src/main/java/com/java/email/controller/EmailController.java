package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.EmailTypeCreateRequest;
import com.java.email.model.EmailTypeFilterRequest;
import com.java.email.model.EmailTypeUpdateRequest;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dictionary")
public class EmailController {

    @Autowired
    private EmailService emailService;

    @PostMapping("/createEmailType")
    public Result<?> createEmail(@RequestBody(required = true) EmailTypeCreateRequest request) {
        System.out.println("Received request: " + request);
        if (request == null || request.getEmail_type_name() == null || request.getEmail_type_name().trim().isEmpty()) {
            return Result.error("邮件类型名称不能为空");
        }
        return emailService.createEmail(request.getEmail_type_name());
    }

    @PostMapping("/filterEmailType")
    public Result<?> filterEmailType(@RequestBody EmailTypeFilterRequest request) {
        return emailService.filterEmailType(request);
    }

    @PostMapping("/updateEmailType")
    public Result<?> updateEmailType(@RequestBody EmailTypeUpdateRequest request) {
        if (request == null) {
            return Result.error("请求参数不能为空");
        }
        if (request.getEmail_type_id() == null || request.getEmail_type_id().trim().isEmpty()) {
            return Result.error("邮件类型ID不能为空");
        }
        if (request.getEmail_type_name() == null || request.getEmail_type_name().trim().isEmpty()) {
            return Result.error("邮件类型名称不能为空");
        }
        return emailService.updateEmailType(request);
    }
}