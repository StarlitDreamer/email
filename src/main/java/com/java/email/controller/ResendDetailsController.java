package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.request.ResendEmailRequest;
import com.java.email.service.ResendDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/emails")
public class ResendDetailsController {

    @Autowired
    private ResendDetailsService resendDetailsService;

    @PostMapping("/resend")
    public Result<String> resendEmail(@RequestBody ResendEmailRequest request) {
        // 拼接 "resend_" 字符串
        String emailResendId = "resend_" + request.getEmailId();

        // 将拼接后的 emailResendId 存入 Redis
        resendDetailsService.saveEmailResendId(emailResendId);

        // 返回响应，包含拼接后的 emailResendId
        return Result.success(emailResendId);
    }
}