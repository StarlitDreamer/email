package com.java.email.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.entity.Attachment;
import lombok.Data;

import java.util.List;

//手动发送
@Data
public class CreateCycleEmailTaskRequest {
    @JsonProperty("")
    private String subject;// 邮件主题

    @JsonProperty("email_type_id")  // 邮件类型ID
    private String emailTypeId;

    @JsonProperty("template_id")// 模板ID
    private String templateId;

    @JsonProperty("receiver_id") // 收件人ID列表
    private List<String> receiverIds;

    @JsonProperty("")
    private List<Attachment> attachments;// 附件列表

    @JsonProperty("send_cycle")
    private long taskCycle; // 发送周期
}