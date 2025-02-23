package com.java.email.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.entity.Attachment;
import lombok.Data;

import java.util.List;

@Data
public class EmailTaskRequest {
    @JsonProperty("subject")
    private String subject;

    @JsonProperty("email_type_id")
    private String emailTypeId;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("email_content")
    private String emailContent;

    @JsonProperty("receiver_id")
    private List<String> receiverId;

    @JsonProperty("receiver_supplier_id")
    private List<String> receiverSupplierId;

    @JsonProperty("receiver_key")
    private String receiverKey;

    @JsonProperty("receiver_supplier_key")
    private String receiverSupplierKey;

    @JsonProperty("cancel_receiver_id")
    private String cancelReceiverId;

    @JsonProperty("attachment")
    private List<Attachment> attachment;

    @JsonProperty("task_type")
    private int taskType;
}