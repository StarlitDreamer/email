package com.java.email.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.entity.Attachment;
import lombok.Data;

import java.util.List;
//创建手动发送
@Data
public class CreateEmailTaskRequest {
    @JsonProperty("")
    private String subject;

    @JsonProperty("email_type_id") // 指定前端字段名
    private String emailTypeId;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("email_content")
    private String emailContent;

    @JsonProperty("receiver_id")
    private List<String> receiverIds;

    @JsonProperty("receiver_key")
    private String receiverKey;//redis_key

    @JsonProperty("cancel_receiver_ids")
    private List<String> cancelReceiverIds;

    @JsonProperty("")
    private List<Attachment> attachments;
}
