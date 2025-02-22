package com.java.email.dto;

import lombok.Data;

@Data
public class EmailRequest {
    private String serverName;
    private String to;
    private String subject;
    private String text;

    // Getters and Setters
}