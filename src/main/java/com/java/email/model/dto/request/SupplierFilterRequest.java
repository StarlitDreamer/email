package com.java.email.model.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class SupplierFilterRequest {
    private String supplierName;
    private String contactPerson;
    private String contactWay;
    private Integer supplierLevel;
    private String supplierCountryId;
    private Integer tradeType;
    private String commodityName;
    private String sex;
    private String birth;
    private String email;
    private List<String> noAcceptEmailTypeId;
    private String belongUserName;
    private String creatorName;
    private Integer status;
    private Integer pageNum;
    private Integer pageSize;
} 