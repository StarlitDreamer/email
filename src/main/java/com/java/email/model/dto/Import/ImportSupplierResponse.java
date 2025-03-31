package com.java.email.model.dto.Import;

import java.util.List;

import lombok.Data;

@Data
public class ImportSupplierResponse {
    private Integer success_count;
    private Integer fail_count;
    private List<String> errorMsg;
}
