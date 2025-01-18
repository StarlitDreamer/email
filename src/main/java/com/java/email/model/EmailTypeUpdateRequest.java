package com.java.email.model;

public class EmailTypeUpdateRequest {
    private String email_type_id;
    private String email_type_name;

    // 基本的 getter 和 setter
    public String getEmail_type_id() {
        return email_type_id;
    }

    public void setEmail_type_id(String email_type_id) {
        this.email_type_id = email_type_id;
    }

    public String getEmail_type_name() {
        return email_type_name;
    }

    public void setEmail_type_name(String email_type_name) {
        this.email_type_name = email_type_name;
    }

    // 添加无参构造函数
    public EmailTypeUpdateRequest() {
    }
} 