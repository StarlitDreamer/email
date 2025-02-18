package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTask {
    /**
     * 附件
     */
    private Attachment[] attachment;

    /**
     * 退信数量
     */
    @JsonProperty("bounce_amount")
    private long bounceAmount;

    /**
     * 创建时间，秒级时间戳
     */
    @JsonProperty("created_at")
    private long createdAt;

    /**
     * 邮件内容
     */
    @JsonProperty("email_content")
    private String emailContent;

    /**
     * 主键 值和业务id一样
     */
    @JsonProperty("email_id")
    private String emailId;

    /**
     * 邮件任务id，使用uuid
     */
    @JsonProperty("email_task_id")
    private String emailTaskId;

    /**
     * 邮件类型id，字典里面的邮件类型id
     */
    @JsonProperty("email_type_id")
    private String emailTypeId;

    /**
     * 结束时间，秒级时间戳
     */
    @JsonProperty("end_date")
    private long endDate;

    /**
     * 循环邮件的发送下标，初始值为0
     */
    private long index;

    /**
     * 循环邮件的间隔时间，秒级时间戳
     */
    @JsonProperty("interval_date")
    private long intervalDate;

    /**
     * 收件人id
     */
    @JsonProperty("receiver_id")
    private String[] receiverId;

    /**
     * 发件人邮箱
     */
    @JsonProperty("sender_id")
    private String[] senderId;

    /**
     * 僵尸用户邮箱号，此版本不用
     */
    @JsonProperty("shadow_id")
    private String[] shadowId;

    /**
     * 开始时间，秒级时间戳
     */
    @JsonProperty("start_date")
    private long startDate;

    /**
     * 主题
     */
    private String subject;

    /**
     * 任务循环周期，如果是循环发送类型的任务才会填写这个字段
     */
    @JsonProperty("task_cycle")
    private Long taskCycle;

    /**
     * 任务类型，0 普通邮件 1 循环邮件 2 定时发送 3
     */
    @JsonProperty("task_type")
    private Integer taskType;

    /**
     * 模板id
     */
    @JsonProperty("template_id")
    private String templateId;

    /**
     * 退订数量
     */
    @JsonProperty("unsubscribe_amount")
    private long unsubscribeAmount;

    @JsonProperty("sender_name")
    private String[] SenderName;

    @JsonProperty("receiver_name")
    private String[] ReceiverName;

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
                .properties("attachment", Property.of(p -> p.nested(n -> n)))
                .properties("bounce_amount", Property.of(p -> p.long_(l -> l)))
                .properties("created_at", Property.of(p -> p.long_(l -> l)))
                .properties("email_content", Property.of(p -> p.text(t -> t
                    .fields("keyword", f -> f.keyword(k -> k)))))
                .properties("email_id", Property.of(p -> p.keyword(k -> k)))
                .properties("email_task_id", Property.of(p -> p.keyword(k -> k)))
                .properties("email_type_id", Property.of(p -> p.keyword(k -> k)))
                .properties("end_date", Property.of(p -> p.long_(l -> l)))
                .properties("index", Property.of(p -> p.long_(l -> l)))
                .properties("interval_date", Property.of(p -> p.long_(l -> l)))
                .properties("receiver_id", Property.of(p -> p.keyword(k -> k)))
                .properties("sender_id", Property.of(p -> p.keyword(k -> k)))
                .properties("shadow_id", Property.of(p -> p.keyword(k -> k)))
                .properties("start_date", Property.of(p -> p.long_(l -> l)))
                .properties("subject", Property.of(p -> p.text(t -> t
                    .fields("keyword", f -> f.keyword(k -> k)))))
                .properties("task_cycle", Property.of(p -> p.long_(l -> l)))
                .properties("task_type", Property.of(p -> p.integer(i -> i)))
                .properties("template_id", Property.of(p -> p.keyword(k -> k)))
                .properties("unsubscribe_amount", Property.of(p -> p.long_(l -> l)))
                .properties("receiver_name", Property.of(p -> p.keyword(k -> k)))
                .properties("sender_name", Property.of(p -> p.keyword(k -> k)))
                .build();
    }
}