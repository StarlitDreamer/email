package com.java.model.dto;

import java.util.List;

import lombok.Data;

@Data
public class ImportUserResponse {
    private Integer success_count;
    private Integer fail_count;
    private List<String> errorMsg;
}