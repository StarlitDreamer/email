package com.java.email.service;

import com.java.email.entity.EmailReport;
import com.java.email.repository.EmailReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailReportService {

    @Autowired
    private EmailReportRepository emailReportRepository;

    /**
     * 根据email_task_id更新退订数量
     *
     * @param emailTaskId 邮件任务ID
     * @return 更新后的EmailReport实体
     */
    public EmailReport updateUnsubscribeAmount(String emailTaskId) {
        // 根据email_task_id查找EmailReport实体
        EmailReport emailReport = emailReportRepository.findByEmailTaskId(emailTaskId)
                .orElseThrow(() -> new RuntimeException("邮件任务报告未找到"));

        // 增加退订数量
        emailReport.setUnsubscribeAmount(emailReport.getUnsubscribeAmount() + 1);

        // 保存更新后的EmailReport实体
        return emailReportRepository.save(emailReport);
    }

     /**
     * 根据email_task_id更新打开数量
     *
     * @param emailTaskId 邮件任务ID
     * @return 更新后的EmailReport实体
     */
    public EmailReport updateOpenAmount(String emailTaskId) {
        // 根据email_task_id查找EmailReport实体
        EmailReport emailReport = emailReportRepository.findByEmailTaskId(emailTaskId)
                .orElseThrow(() -> new RuntimeException("邮件任务报告未找到"));

        // 增加打开数量
        emailReport.setOpenAmount(emailReport.getOpenAmount() + 1);

        // 保存更新后的EmailReport实体
        return emailReportRepository.save(emailReport);
    }

}
