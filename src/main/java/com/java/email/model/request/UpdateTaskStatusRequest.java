package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateTaskStatusRequest {
    @JsonProperty("email_task_id")
    private String emailTaskId;

    @JsonProperty("operate_status")
    private Integer OperateStatus;
}
