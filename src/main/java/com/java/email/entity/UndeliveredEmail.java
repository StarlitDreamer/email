package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * 未送达邮件实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "undelivered_email")
public class UndeliveredEmail {
    @Id
    private String emailId;             // 邮件ID，使用UUID
    private String emailTaskId;         // 邮件任务ID，使用UUID
    private long createdAt;             // 创建时间，秒级时间戳
    private long startDate;             // 开始时间，秒级时间戳
    private long endDate;               // 结束时间，秒级时间戳
    private Long errorCode;             // 错误代码，status为2或3时再填写
    private String errorMsg;            // 错误信息，status为2或3时再填写
    private List<String> receiverIds;   // 收件人ID列表
    private List<String> senderIds;     // 发件人ID列表
    private String resendCode;          // 重发错误代码，重发失败再填写
    private Long resendStartDate;       // 重发开始时间，秒级时间戳
    private Long resendEndDate;         // 重发结束时间，秒级时间戳
    private String resendMsg;           // 重发错误信息，重发失败再填写
    private int resendStatus;           // 邮件重发状态，使用数字1、2、3、4代表。1是未重发、2是发送中、3是重发成功、4是重发失败
    private ResendStrategy resendStrategy; // 重发策略

    /**
     * 设置邮件ID。
     *
     * @param value 邮件ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果邮件ID为 null 或空。
     */
    public void setEmailId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Email ID cannot be null or empty");
        }
        this.emailId = value;
    }

    /**
     * 设置邮件任务ID。
     *
     * @param value 邮件任务ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果邮件任务ID为 null 或空。
     */
    public void setEmailTaskId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Email task ID cannot be null or empty");
        }
        this.emailTaskId = value;
    }

    /**
     * 设置邮件重发状态。
     *
     * @param value 邮件重发状态，取值范围为1到4。
     * @throws IllegalArgumentException 如果邮件重发状态不在有效范围内。
     */
    public void setResendStatus(int value) {
        if (value < 1 || value > 4) {
            throw new IllegalArgumentException("Resend status must be between 1 and 4");
        }
        this.resendStatus = value;
    }
}
