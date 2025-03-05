package com.java.email.model.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UnsubscribeRequest {
    @JsonProperty("email_id")
    private String emailId;

    @JsonProperty("receiver_email")
    private String receiverEmail;
}