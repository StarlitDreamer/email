package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.java.email.model.entity.Attachment;
import lombok.Data;

import java.util.List;

@Data
public class UpdateBirthEmailTaskRequest {
    @JsonProperty("operate_status")
    private String emailStatus;

    @JsonProperty("subject")
    private String subject;

    @JsonProperty("template_id")
    private String templateId;

    @JsonProperty("attachment")
    private List<Attachment> attachment;
}