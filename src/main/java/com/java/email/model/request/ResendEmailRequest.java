package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResendEmailRequest {
    @JsonProperty("email_id")
    private String emailId;
}
