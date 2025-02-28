package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {
    @JsonProperty("task_id")
    private String taskId;

    @JsonProperty("operate_status")
    private String OperateStatus;
}
