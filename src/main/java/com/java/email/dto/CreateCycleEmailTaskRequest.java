package com.java.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.entity.Attachment;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

//创建循环发送
@Data
public class CreateCycleEmailTaskRequest {
    @JsonProperty("attachment")
    @Field(name = "attachment", type = FieldType.Object)
    private List<Attachment> attachment;  // 附件

    @JsonProperty("email_type_id")
    @Field(name = "email_type_id", type = FieldType.Keyword)
    private String emailTypeId;           // 邮件类型ID

    @JsonProperty("receiver_id")
    @Field(name = "receiver_id", type = FieldType.Keyword)
    private List<String> receiverId;      // 收件人ID列表

    @JsonProperty("receiver_key")
    @Field(name = "receiver_key", type = FieldType.Keyword)
    private String receiverKey;           // 收件人Key

    @JsonProperty("receiver_supplier_id")
    @Field(name = "receiver_supplier_id", type = FieldType.Keyword)
    private List<String> receiverSupplierId;  // 收件人供应商ID列表

    @JsonProperty("receiver_supplier_key")
    @Field(name = "receiver_supplier_key", type = FieldType.Keyword)
    private String receiverSupplierKey;   // 收件人供应商Key

    @JsonProperty("send_cycle")
    @Field(name = "send_cycle", type = FieldType.Long)
    private long sendCycle;               // 发送周期，单位为秒

    @JsonProperty("subject")
    @Field(name = "subject", type = FieldType.Text)
    private String subject;               // 邮件主题

    @JsonProperty("template_id")
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;            // 模板ID

    @JsonProperty("sender_id")
    @Field(name = "sender_id", type = FieldType.Keyword)
    private String senderId;               //发送者id

    @JsonProperty("index")
    @Field(name = "index", type = FieldType.Long)
    private Long index;

    @JsonProperty("interval_date")
    @Field(name = "interval_date", type = FieldType.Long)
    private Integer intervalDate;//任务的间隔时间，秒级时间戳
}