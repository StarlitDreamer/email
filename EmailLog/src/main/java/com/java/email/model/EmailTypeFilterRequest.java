package com.java.email.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailTypeFilterRequest {
    private String email_type_name;
    private Integer page_num = 1;
    private Integer page_size = 10;

    // 手动添加 getter/setter 方法
    public String getEmail_type_name() {
        return email_type_name;
    }

    public void setEmail_type_name(String email_type_name) {
        this.email_type_name = email_type_name;
    }

    public Integer getPage_num() {
        return page_num;
    }

    public void setPage_num(Integer page_num) {
        this.page_num = page_num;
    }

    public Integer getPage_size() {
        return page_size;
    }

    public void setPage_size(Integer page_size) {
        this.page_size = page_size;
    }
} 