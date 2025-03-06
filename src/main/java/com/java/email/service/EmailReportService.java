package com.java.email.service;

import com.java.email.model.entity.EmailDetail;
import com.java.email.model.entity.EmailReport;
import com.java.email.model.request.OpenRequest;
import com.java.email.repository.EmailDetailRepository;
import com.java.email.repository.EmailReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmailReportService {

    @Autowired
    private EmailReportRepository emailReportRepository;

    @Autowired
    private EmailDetailRepository emailDetailRepository;

    /**
     * 根据email_task_id更新退订数量
     *
     * @param emailTaskId 邮件任务ID
     * @return 更新后的EmailReport实体
     */
    public EmailReport updateUnsubscribeAmount(String emailTaskId) {
        // 根据email_task_id查找EmailReport实体
        EmailReport emailReport = emailReportRepository.findByEmailTaskId(emailTaskId);
        if (emailReport == null) {
            throw new RuntimeException("邮件任务报告未找到");
        }

        // 增加退订数量
        emailReport.setUnsubscribeAmount(emailReport.getUnsubscribeAmount() + 1);

        // 保存更新后的EmailReport实体
        return emailReportRepository.save(emailReport);
    }


    /**
     * 根据email_task_id更新打开数量
     *
     * @param request 邮件任务ID
     * @return 更新后的EmailReport实体
     */
    public EmailReport updateOpenAmount(OpenRequest request) {
        String emailTaskId = request.getEmailTaskId();
        String receiverEmail = request.getReceiverEmail();

        EmailDetail emailDetail = emailDetailRepository.findByEmailTaskIdAndReceiverId(emailTaskId, receiverEmail);
        EmailReport emailReport = emailReportRepository.findByEmailTaskId(emailTaskId);

        if (emailDetail.getOpened() == 0) {
            emailDetail.setOpened(1);
        } else {
            throw new RuntimeException("邮件已经打开");
        }
        emailDetailRepository.save(emailDetail);

        if (emailReport == null) {
            emailReport = new EmailReport();
            emailReport.setEmailTaskId(emailTaskId);
            emailReport.setUnsubscribeAmount(1L);
            emailReportRepository.save(emailReport);
        } else {
            emailReport.setUnsubscribeAmount(emailReport.getUnsubscribeAmount() + 1);
            emailReportRepository.save(emailReport);
        }

        return emailReport;
    }
}
