package com.java.email.model;

import lombok.Data;

@Data
public class EmailTypeRequest {
    private String emailTypeName;

    // getter and setter
    public String getEmailTypeName() {
        return emailTypeName;
    }

    public void setEmailTypeName(String emailTypeName) {
        this.emailTypeName = emailTypeName;
    }
}