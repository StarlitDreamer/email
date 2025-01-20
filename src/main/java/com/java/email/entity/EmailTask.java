package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * 邮件任务管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email_tasks")
public class EmailTask {
    @Id
    private String emailTaskId;         // 邮件任务ID，使用UUID
    private List<Attachment> attachments; // 附件列表
    private long bounceAmount;          // 退信数量
    private long createdAt;             // 创建时间，秒级时间戳
    private String emailContent;        // 邮件内容
    private String emailTypeId;         // 邮件类型ID，字典里面的邮件类型ID
    private long endDate;               // 结束时间，秒级时间戳
    private int operateStatus;          // 任务操作状态，使用数字1、2、3、4代表状态。1是开始态、2是暂停态、3是终止态、4是重置态
    private List<String> receiverIds;   // 收件人ID列表
    private List<String> cancelReceiverIds;//取消收件的收件人ID列表             新加
    private String receiverKey;         //收件人在redis中的key                 新加
    private List<String> senderIds;     // 发件人ID列表
    private long startDate;             // 开始时间，秒级时间戳
    private String subject;             // 主题
    private long taskCycle;             // 任务循环周期，如果是循环发送类型的任务才会填写这个字段
    private int taskStatus;             // 任务状态，使用数字1、2、3、4、5、6代表任务状态。1是发送中、2是发送暂停、3是发送终止、4是发送重置、5是发送成功、6是发送失败
    private int taskType;               // 任务类型，使用数字1、2、3、4代表任务类型。1是手动发送、2是循环发送、3是节日发送、4是生日发送
    private String templateId;          // 模板ID
    private long unsubscribeAmount;     // 退订数量

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
     * 设置邮件类型ID。
     *
     * @param value 邮件类型ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果邮件类型ID为 null 或空。
     */
    public void setEmailTypeId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Email type ID cannot be null or empty");
        }
        this.emailTypeId = value;
    }

    /**
     * 设置模板ID。
     *
     * @param value 模板ID，不能为 null 或空。
     * @throws IllegalArgumentException 如果模板ID为 null 或空。
     */
    public void setTemplateId(String value) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException("Template ID cannot be null or empty");
        }
        this.templateId = value;
    }

    /**
     * 设置任务操作状态。
     *
     * @param value 任务操作状态，取值范围为1到4。
     * @throws IllegalArgumentException 如果任务操作状态不在有效范围内。
     */
    public void setOperateStatus(int value) {
        if (value < 1 || value > 4) {
            throw new IllegalArgumentException("Operate status must be between 1 and 4");
        }
        this.operateStatus = value;
    }

    /**
     * 设置任务状态。
     *
     * @param value 任务状态，取值范围为1到6。
     * @throws IllegalArgumentException 如果任务状态不在有效范围内。
     */
    public void setTaskStatus(int value) {
        if (value < 1 || value > 6) {
            throw new IllegalArgumentException("Task status must be between 1 and 6");
        }
        this.taskStatus = value;
    }

    /**
     * 设置任务类型。
     *
     * @param value 任务类型，取值范围为1到4。
     * @throws IllegalArgumentException 如果任务类型不在有效范围内。
     */
    public void setTaskType(int value) {
        if (value < 1 || value > 4) {
            throw new IllegalArgumentException("Task type must be between 1 and 4");
        }
        this.taskType = value;
    }
}
