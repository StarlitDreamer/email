package com.java.email.controller;

import com.java.email.entity.ResendDetails;
import com.java.email.service.ResendDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email/resend")
public class ResendDetailsController {

    @Autowired
    private ResendDetailsService resendDetailsService;

    /**
     * 接受email_task_id，创建重发记录
     */
    @PostMapping
    public ResendDetails createResendDetails(@RequestParam String emailTaskId) {
        return resendDetailsService.createResendDetails(emailTaskId);
    }
}
