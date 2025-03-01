package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.model.entity.EmailReport;
import com.java.email.service.EmailReportService;
import com.java.email.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/email-report")
public class EmailReportController {

    @Autowired
    private EmailReportService emailReportService;

    @Autowired
    private EmailService emailService;
    /**
     * 用户点击退订链接，根据email_task_id增加退订数量。
     *
     * @param emailTaskId 邮件任务ID
     * @return 返回更新结果
     */
    @PutMapping("/unsubscribe")
    public Result updateUnsubscribeAmount(@RequestParam String emailTaskId, @RequestParam String receiverEmail) {
        String customerIdOrSupplierIdByEmail = emailService.findCustomerOrSupplierByEmail(receiverEmail);
        System.out.println(customerIdOrSupplierIdByEmail);
        try {
            EmailReport updatedEmailReport = emailReportService.updateUnsubscribeAmount(emailTaskId,receiverEmail);
            return Result.success(updatedEmailReport);
        } catch (Exception e) {
            return Result.error("更新退订数量失败: " + e.getMessage());
        }
    }

    /**
     * 根据email_task_id更新打开数量
     *
     * @param emailTaskId 邮件任务ID
     * @return 更新后的EmailReport实体
     */
    @PutMapping("/open-email/{emailTaskId}")
    public Result updateOpenAmount(@PathVariable String emailTaskId) {
        try {
            EmailReport updatedEmailReport = emailReportService.updateOpenAmount(emailTaskId);
            return Result.success(updatedEmailReport);
        } catch (Exception e) {
            return Result.error("更新打开数量失败: " + e.getMessage());
        }
    }

}
