package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.model.entity.Attachment;
import lombok.Data;

import java.util.List;

@Data
public class CreateEmailTaskRequest {
    @JsonProperty("subject")
    private String subject;                 // 邮件主题

    @JsonProperty("email_type_id")
    private String emailTypeId;             // 邮件类型id

    @JsonProperty("template_id")
    private String templateId;              // 模板id

    @JsonProperty("email_content")
    private String emailContent;            // 邮件内容

    @JsonProperty("receiver_id")
    private List<String> receiverId;        // 客户id

    @JsonProperty("receiver_supplier_id")
    private List<String> receiverSupplierId; // 供应商id

    @JsonProperty("receiver_key")
    private String receiverKey;             // 全选客户key

    @JsonProperty("receiver_supplier_key")
    private String receiverSupplierKey;     // 全选供应商key

    @JsonProperty("cancel_receiver_id")
    private List<String> cancelReceiverId;  // 取消收件人id

    @JsonProperty("attachment")
    private List<Attachment> attachment;    // 附件
}
