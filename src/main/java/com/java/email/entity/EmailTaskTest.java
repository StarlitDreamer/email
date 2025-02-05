package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.ArrayList;

@Document(indexName = "your_index")
public class EmailTaskTest {

    @Id
    @JsonProperty("email_id")
    private String emailId; // 邮件id

    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId; // 邮件任务id

    @JsonProperty("email_type_id")
    @Field(name = "email_type_id", type = FieldType.Keyword)
    private String emailTypeId; // 邮件类型id

    @JsonProperty("task_type")
    @Field(name = "task_type", type = FieldType.Integer)
    private Integer taskType;  // 任务类型

    @JsonProperty("task_cycle")
    @Field(name = "task_cycle", type = FieldType.Integer)
    private Integer taskCycle; // 任务循环周期

    @JsonProperty("sender_id")
    @Field(name = "sender_id", type = FieldType.Keyword)
    private String senderId; // 发件人id

    @JsonProperty("shadow_id")
    @Field(name = "shadow_id", type = FieldType.Keyword)
    private ArrayList<String> shadowId; // 影子用户id

    @JsonProperty("receiver_id")
    @Field(name = "receiver_id", type = FieldType.Keyword)
    private ArrayList<String> receiverId; // 收件人id

    @JsonProperty("attachment")
    @Field(name = "attachment", type = FieldType.Keyword)
    private String attachment; // 附件的url

    @JsonProperty("template_id")
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId; // 模板id

    @JsonProperty("subject")
    @Field(name = "subject", type = FieldType.Keyword)
    private String subject; // 邮件主题

    @JsonProperty("email_content")
    @Field(name = "email_content", type = FieldType.Keyword)
    private String emailContent; // 邮件内容

    @JsonProperty("bounce_amount")
    @Field(name = "bounce_amount", type = FieldType.Integer)
    private Integer bounceAmount; // 退信数量

    @JsonProperty("unsubscribe_amount")
    @Field(name = "unsubscribe_amount", type = FieldType.Integer)
    private Integer unsubscribeAmount; // 退订数量

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt; // 任务创建时间

    @JsonProperty("start_date")
    @Field(name = "start_date", type = FieldType.Long)
    private Long startDate; // 邮件任务开始时间

    @JsonProperty("end_date")
    @Field(name = "end_date", type = FieldType.Long)
    private Long endDate; // 邮件任务的结束时间

    @JsonProperty("interval_date")
    @Field(name = "interval_date", type = FieldType.Integer)
    private Integer intervalDate; // 任务的间隔时间，秒级时间戳

    @JsonProperty("index")
    @Field(name = "index", type = FieldType.Integer)
    private Integer index; // 循环任务的发送下标
}
