package com.java.email.model;

import lombok.Data;

@Data
public class EmailTypeFilterRequest {
    private String emailTypeName;
    private Integer pageNum;
    private Integer pageSize;
}