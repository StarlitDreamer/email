package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OpenRequest {
    @JsonProperty("email_id")
    private String emailId;
}
