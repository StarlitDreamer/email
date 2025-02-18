package com.java.email.model;

public class EmailTypeCreateRequest {
    private String email_type_name;

    // 基本的 getter 和 setter
    public String getEmail_type_name() {
        return email_type_name;
    }

    public void setEmail_type_name(String email_type_name) {
        this.email_type_name = email_type_name;
    }

    // 添加无参构造函数
    public EmailTypeCreateRequest() {
    }

    // 添加带参构造函数
    public EmailTypeCreateRequest(String email_type_name) {
        this.email_type_name = email_type_name;
    }
} 