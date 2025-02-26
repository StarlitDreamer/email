package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.model.entity.Attachment;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
public class CreateEmailTaskRequest {
    @JsonProperty("attachment")
    @Field(name = "attachment", type = FieldType.Object)
    private List<Attachment> attachment;    // 附件

    @JsonProperty("cancel_receiver_id")
    @Field(name = "cancel_receiver_id", type = FieldType.Keyword)
    private List<String> cancelReceiverId;  // 取消收件人id

    @JsonProperty("email_content")
    @Field(name = "email_content", type = FieldType.Text)
    private String emailContent;            // 邮件内容

    @JsonProperty("email_type_id")
    @Field(name = "email_type_id", type = FieldType.Keyword)
    private String emailTypeId;             // 邮件类型id

    @JsonProperty("receiver_id")
    @Field(name = "receiver_id", type = FieldType.Keyword)
    private List<String> receiverId;        // 客户id

    @JsonProperty("receiver_key")
    @Field(name = "receiver_key", type = FieldType.Keyword)
    private String receiverKey;             // 全选客户key

    @JsonProperty("receiver_supplier_id")
    @Field(name = "receiver_supplier_id", type = FieldType.Keyword)
    private List<String> receiverSupplierId; // 供应商id

    @JsonProperty("receiver_supplier_key")
    @Field(name = "receiver_supplier_key", type = FieldType.Keyword)
    private String receiverSupplierKey;     // 全选供应商key

    @JsonProperty("subject")
    @Field(name = "subject", type = FieldType.Text)
    private String subject;                 // 邮件主题

    @JsonProperty("template_id")
    @Field(name = "template_id", type = FieldType.Keyword)
    private String templateId;              // 模板id

    @JsonProperty("sender_id")
    @Field(name = "sender_id", type = FieldType.Keyword)
    private String senderId;              // 发送者id

}
