package com.java.email.service;

import com.java.email.entity.EmailDetail;
import com.java.email.entity.EmailReport;
import com.java.email.repository.EmailDetailRepository;
import com.java.email.repository.EmailReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmailDetailService {

    @Autowired
    private EmailDetailRepository emailDetailRepository;


    @Autowired
    private EmailReportRepository emailReportRepository;

    // 获取所有状态码为 500 的邮件的 emailTaskId
    public List<String> getEmailTaskIdsForErrorCode500() {
        List<EmailDetail> emailDetails = emailDetailRepository.findByErrorCode(500);
        return emailDetails.stream()
                .map(EmailDetail::getEmailTaskId)
                .collect(Collectors.toList());
    }

    // 获取退信数量
    public long getBounceCount() {
        return emailDetailRepository.countByErrorCode(500);
    }

    // 获取送达数量
    public long getDeliveredCount() {
        return emailDetailRepository.countByErrorCode(200);
    }

    // 每隔一小时执行一次统计，并将结果保存到 email_report 索引
    @Scheduled(cron = "0 * * * * ?")  // 每小时执行一次
    public void generateEmailReport() {
        long bounceCount = getBounceCount();
        long deliveredCount = getDeliveredCount();
//        long currentTime = System.currentTimeMillis() / 1000;  // 获取当前时间（秒级时间戳）

        // 创建报告
        EmailReport emailReport = new EmailReport();
//        emailReport.setReportTime(currentTime);
        emailReport.setBounceAmount(bounceCount);
        emailReport.setDeliveryAmount(deliveredCount);

        // 保存到 email_report 索引
        emailReportRepository.save(emailReport);

        // 打印日志，确认任务执行
        System.out.println("Email report generated. Bounce Count: " + bounceCount + ", Delivered Count: " + deliveredCount);
    }
}
