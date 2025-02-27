package com.java.email.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEmailsBySupplierIdsResponse {
    private String supplierId;
    private String supplierName;
    private List<String> supplierEmails;
}
