package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * 邮件管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email")
public class Email {
    @Id
    @Field(name = "email_id", type = FieldType.Keyword)
    private String emailId;              // 邮件ID，使用UUID，不分词

    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;          // 邮件任务ID，使用UUID，不分词

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;              // 创建时间，秒级时间戳

    @JsonProperty("update_at")
    @Field(name = "update_at", type = FieldType.Long)
    private Long updateAt;               // 状态修改时间，秒级时间戳

    @JsonProperty("email_status")
    @Field(name = "email_status", type = FieldType.Integer)
    private Integer emailStatus;         // 邮件状态：1-开始，2-暂停，3-终止，4-重置，5-异常，6-完成
//    @Id
//    private String emailId;             // 邮件ID，使用UUID
//    private long createdAt;             // 创建时间，秒级时间戳
//    private long startDate;             // 开始时间，秒级时间戳
//    private long endDate;               // 结束时间，秒级时间戳
//    private int emailStatus;            // 邮件状态，使用数字1、2、3、4、5代表。1是已送达、2是已打开、3是未送达、4是已退信、5是已退订。
//    private String emailTaskId;         // 邮件任务ID，使用UUID
//    private int errorCode;             // 错误代码，status为2或3时再填写
//    private String errorMsg;            // 错误信息，status为2或3时再填写
//    private List<String> receiverIds;   // 收件人ID列表
//    private List<String> senderIds;     // 发件人ID列表

//    /**
//     * 设置邮件ID。
//     *
//     * @param value 邮件ID，不能为 null 或空。
//     * @throws IllegalArgumentException 如果邮件ID为 null 或空。
//     */
//    public void setEmailId(String value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Email ID cannot be null or empty");
//        }
//        this.emailId = value;
//    }
//
//    /**
//     * 设置邮件任务ID。
//     *
//     * @param value 邮件任务ID，不能为 null 或空。
//     * @throws IllegalArgumentException 如果邮件任务ID为 null 或空。
//     */
//    public void setEmailTaskId(String value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Email task ID cannot be null or empty");
//        }
//        this.emailTaskId = value;
//    }
//
//    /**
//     * 设置邮件状态。
//     *
//     * @param value 邮件状态，取值范围为1到5。
//     * @throws IllegalArgumentException 如果邮件状态不在有效范围内。
//     */
//    public void setEmailStatus(int value) {
//        if (value < 1 || value > 5) {
//            throw new IllegalArgumentException("Email status must be between 1 and 5");
//        }
//        this.emailStatus = value;
//    }
//
//    /**
//     * 设置收件人ID列表。
//     *
//     * @param value 收件人ID列表，不能为 null 或空。
//     * @throws IllegalArgumentException 如果收件人ID列表为 null 或空。
//     */
//    public void setReceiverIds(List<String> value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Receiver IDs cannot be null or empty");
//        }
//        this.receiverIds = value;
//    }
//
//    /**
//     * 设置发件人ID列表。
//     *
//     * @param value 发件人ID列表，不能为 null 或空。
//     * @throws IllegalArgumentException 如果发件人ID列表为 null 或空。
//     */
//    public void setSenderIds(List<String> value) {
//        if (value == null || value.isEmpty()) {
//            throw new IllegalArgumentException("Sender IDs cannot be null or empty");
//        }
//        this.senderIds = value;
//    }
//
//    /**
//     * 设置创建时间。
//     *
//     * @param value 创建时间，秒级时间戳。
//     * @throws IllegalArgumentException 如果创建时间为负数。
//     */
//    public void setCreatedAt(long value) {
//        if (value < 0) {
//            throw new IllegalArgumentException("Created at timestamp cannot be negative");
//        }
//        this.createdAt = value;
//    }
//
//    /**
//     * 设置开始时间。
//     *
//     * @param value 开始时间，秒级时间戳。
//     * @throws IllegalArgumentException 如果开始时间为负数。
//     */
//    public void setStartDate(long value) {
//        if (value < 0) {
//            throw new IllegalArgumentException("Start date timestamp cannot be negative");
//        }
//        this.startDate = value;
//    }
//
//    /**
//     * 设置结束时间。
//     *
//     * @param value 结束时间，秒级时间戳。
//     * @throws IllegalArgumentException 如果结束时间为负数。
//     */
//    public void setEndDate(long value) {
//        if (value < 0) {
//            throw new IllegalArgumentException("End date timestamp cannot be negative");
//        }
//        this.endDate = value;
//    }
}
