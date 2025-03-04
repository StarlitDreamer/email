package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailBeginRequest {
    @JsonProperty("email_task_id")
    private String emailTaskId;          // 邮件任务ID，使用UUID，不分词
}
