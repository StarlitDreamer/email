package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResetTaskStatusRequest {
    @JsonProperty("task_id")
    private String taskId;
}
