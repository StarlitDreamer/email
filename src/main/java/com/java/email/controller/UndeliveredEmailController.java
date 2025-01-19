package com.java.email.controller;

import com.java.email.common.Result;
import com.java.email.entity.UndeliveredEmail;
import com.java.email.service.UndeliveredEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/undelivered-emails")
public class UndeliveredEmailController {

    @Autowired
    private UndeliveredEmailService undeliveredEmailService;

    /**
     * 根据条件筛选未送达邮件
     *
     * @param emailTaskId   邮件任务 ID
     * @param receiverIds   收件人 ID 列表
     * @param senderIds     发件人 ID 列表
     * @param resendStatus  重发状态
     * @param errorCode     错误代码
     * @param page         页码
     * @param size         每页大小
     * @return 符合条件的未送达邮件分页结果
     */
    @GetMapping("/search")
    public Result<Page<UndeliveredEmail>> findUndeliveredEmailsByCriteria(
            @RequestParam(required = false) String emailTaskId,
            @RequestParam(required = false) List<String> receiverIds,
            @RequestParam(required = false) List<String> senderIds,
            @RequestParam(required = false) Integer resendStatus,
            @RequestParam(required = false) Long errorCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return undeliveredEmailService.findUndeliveredEmailsByCriteria(
                emailTaskId, receiverIds, senderIds, resendStatus, errorCode, page, size);
    }

    /**
     * 分页查询未送达邮件数据
     *
     * @param pageNum  当前页码（从 0 开始）
     * @param pageSize 每页大小
     * @return 当前页的数据列表
     */
    @GetMapping
    public Result<List<UndeliveredEmail>> getUndeliveredEmails(
            @RequestParam(defaultValue = "0") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        List<UndeliveredEmail> undeliveredEmails = undeliveredEmailService.getUndeliveredEmails(pageNum, pageSize);
        return Result.success(undeliveredEmails);
    }

    /**
     * 根据邮件 ID 重新分配重发间隔和重发次数
     *
     * @param emailId     邮件 ID
     * @param resendGap   重发间隔（分钟）
     * @param resendTimes 重发次数
     * @return 操作结果
     */
    @PutMapping("/{emailId}/resend-strategy")
    public Result updateResendStrategy(
            @PathVariable String emailId,
            @RequestParam long resendGap,
            @RequestParam long resendTimes) {
        try {
            UndeliveredEmail updatedEmail = undeliveredEmailService.updateResendStrategy(emailId, resendGap, resendTimes);
            return Result.success(updatedEmail);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新重发策略失败");
        }
    }


    /**
     * 根据邮件 ID 更新重发状态为 2（发送中）
     *
     * @param emailId 邮件 ID
     * @return 操作结果
     */
    @PutMapping("/{emailId}/resend-status")
    public Result updateResendStatus(@PathVariable String emailId) {
        try {
            UndeliveredEmail updatedEmail = undeliveredEmailService.updateResendStatus(emailId, 2);
            return Result.success(updatedEmail);
        } catch (IllegalArgumentException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新重发状态失败");
        }
    }
}