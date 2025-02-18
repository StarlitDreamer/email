package com.java.email.model;

public class EmailTypeDeleteRequest {
    private String email_type_id;

    // 基本的 getter 和 setter
    public String getEmail_type_id() {
        return email_type_id;
    }

    public void setEmail_type_id(String email_type_id) {
        this.email_type_id = email_type_id;
    }

    // 添加无参构造函数
    public EmailTypeDeleteRequest() {
    }
} 