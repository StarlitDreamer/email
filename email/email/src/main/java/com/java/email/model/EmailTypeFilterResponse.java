package com.java.email.model;

import lombok.Data;
import java.util.List;

@Data
public class EmailTypeFilterResponse {
    private Long totalItems;
    private Integer pageNum;
    private Integer pageSize;
    private List<EmailTypeVO> emailType;
}