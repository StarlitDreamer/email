package com.java.email.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.entity.Attachment;
import com.java.email.entity.Receiver;
import lombok.Data;

import java.util.List;

@Data
public class CreateFestivalEmailTaskRequest {
    @JsonProperty("subject")
    private String subject;                // 邮件主题

    @JsonProperty("template_id")
    private String templateId;             // 模板ID

    @JsonProperty("start_date")
    private long startDate;                // 开始时间，秒级时间戳

    @JsonProperty("receiver")
    private List<Receiver> receivers;      // 收件人列表

    @JsonProperty("attachment")
    private List<Attachment> attachments;  // 附件列表
}