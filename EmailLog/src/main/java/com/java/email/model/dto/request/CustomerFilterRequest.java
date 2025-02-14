package com.java.email.model.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class CustomerFilterRequest {
    private Integer pageNum = 1;
    private Integer pageSize = 10;
    private String customerName;
    private String creatorName;
    private String belongUserName;
    private String contactPerson;
    private String contactWay;
    private Integer customerLevel;
    private Integer tradeType;
    private String customerCountryId;
    private String commodityName;
    private String sex;
    private String birth;
    private String email;
    private List<String> acceptEmailTypeId;
    private Integer status;
} 