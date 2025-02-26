package com.java.email.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * 邮件任务管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email_task")
public class EmailTask {
    @JsonProperty("email_id")
    @Field(name = "email_id", type = FieldType.Keyword)
    private String emailId;               // 邮件ID，使用UUID，不分词

    @Id
    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;           // 邮件任务ID，使用UUID，不分词

    @JsonProperty("email_type_id")
    @Field(name = "email_type_id", type = FieldType.Keyword)
    private String emailTypeId;           // 邮件类型ID，字典里的邮件类型ID，不分词

    @JsonProperty("subject")
    @Field(name = "subject", type = FieldType.Text)
    private String subject;               // 邮件主题，支持分词

    @JsonProperty("email_content")
    @Field(name = "email_content", type = FieldType.Text)
    private String emailContent;          // 邮件内容，支持全文检索

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;               // 创建时间，秒级时间戳

    @JsonProperty("start_date")
    @Field(name = "start_date", type = FieldType.Long)
    private Long startDate;               // 开始时间，秒级时间戳

    @JsonProperty("end_date")
    @Field(name = "end_date", type = FieldType.Long)
    private Long endDate;                 // 结束时间，秒级时间戳

    @JsonProperty("task_cycle")
    @Field(name = "task_cycle", type = FieldType.Long)
    private Long taskCycle;               // 任务循环周期，循环发送任务填写

    @JsonProperty("task_type")
    @Field(name = "task_type", type = FieldType.Integer)
    private Integer taskType;             // 任务类型：1-普通邮件，2-循环邮件，3-定时发送，4-生日发送

    @JsonProperty("index")
    @Field(name = "index", type = FieldType.Long)
    private Long index;                   // 循环邮件的发送下标，初始值为0

    @JsonProperty("interval_date")
    @Field(name = "interval_date", type = FieldType.Long)
    private Long intervalDate;            // 循环邮件的间隔时间，秒级时间戳

    @JsonProperty("receiver_id")
    @Field(name = "receiver_id", type = FieldType.Keyword)
    private List<String> receiverId;      // 收件人ID列表，不分词

    @JsonProperty("receiver_name")
    @Field(name = "receiver_name", type = FieldType.Text)
    private List<String> receiverName;    // 收件人姓名列表，支持分词

    @JsonProperty("sender_id")
    @Field(name = "sender_id", type = FieldType.Keyword)
    private String senderId;              // 发件人ID，不分词

    @JsonProperty("sender_name")
    @Field(name = "sender_name", type = FieldType.Text)
    private String senderName;            // 发送者姓名，支持分词

    @JsonProperty("attachment")
    @Field(name = "attachment", type = FieldType.Nested)
    private List<Attachment> attachment;  // 附件列表，包含多个附件

    @JsonProperty("bounce_amount")
    @Field(name = "bounce_amount", type = FieldType.Long)
    private Long bounceAmount;            // 退信数量

    @JsonProperty("unsubscribe_amount")
    @Field(name = "unsubscribe_amount", type = FieldType.Long)
    private Long unsubscribeAmount;       // 退订数量

    @JsonProperty("shadow_id")
    @Field(name = "shadow_id", type = FieldType.Keyword)
    private String shadowId;              // 僵尸用户邮箱号，此版本不用，不分词

    @JsonProperty("template_id")
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;            // 模板ID，不分词

    @JsonProperty("receiver_supplier_id")
    @Field(name = "receiver_supplier_id", type = FieldType.Keyword)
    private List<String> receiverSupplierId;   //

    @JsonProperty("receiver_key")
    @Field(name = "receiver_key", type = FieldType.Keyword)
    private String receiverKey;           //

    @JsonProperty("receiver_supplier_key")
    @Field(name = "receiver_supplier_key", type = FieldType.Keyword)
    private String receiverSupplierKey;   //

    @JsonProperty("cancel_receiver_id")
    @Field(name = "cancel_receiver_id", type = FieldType.Keyword)
    private String cancelReceiverId;  //
}