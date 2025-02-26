package com.java.email.service;

import com.java.email.pojo.EmailReport;

import java.io.IOException;

public interface EmailReportService {

    /**
     * 根据指定的电子邮件任务ID获取对应的邮件报告
     *
     * @param emailTaskId 电子邮件任务的唯一标识符。该ID用于在系统中唯一标识一个已创建的邮件发送任务，
     *                    通常由创建邮件任务时返回的ID值提供
     * @return EmailReport 包含邮件发送详细报告的对象实例。该对象通常包括发送状态（成功/失败）、
     *                     收件人列表、发送时间戳、失败原因（如有）等邮件任务执行结果数据。
     *                     当对应任务不存在时可能返回null
     */
    EmailReport getEmailReport(String emailTaskId) throws IOException;

}
