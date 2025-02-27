package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.model.entity.Attachment;
import lombok.Data;

import java.util.List;

//创建循环发送
@Data
public class CreateFestivalEmailTaskRequest {
    @JsonProperty("subject")
    private String subject;               // 邮件主题

    @JsonProperty("email_type_id")
    private String emailTypeId;           // 邮件类型ID

    @JsonProperty("template_id")
    private String templateId;            // 模板ID

    @JsonProperty("start_date")
    private  Integer startDate;

    @JsonProperty("receiver_id")
    private List<String> receiverId;      // 收件人ID列表

    @JsonProperty("receiver_supplier_id")
    private List<String> receiverSupplierId;  // 收件人供应商ID列表

    @JsonProperty("receiver_key")
    private String receiverKey;           // 收件人Key

    @JsonProperty("receiver_supplier_key")
    private String receiverSupplierKey;   // 收件人供应商Key

    @JsonProperty("attachment")
    private List<Attachment> attachment;  // 附件
}