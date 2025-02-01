package com.java.email.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailTypeVO {
    private String email_type_id;
    private String email_type_name;
    private String created_at;    // ISO 格式的创建时间
    private String updated_at;    // ISO 格式的更新时间

    // 手动添加 getter/setter 方法
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

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }
} 