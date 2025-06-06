package com.java.email.service.receiver;

import com.java.email.common.Response.Result;
import com.java.email.model.entity.receiver.SupplierDocument;
import com.java.email.model.dto.request.SupplierFilterRequest;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface SupplierService {

    Result createSupplier(SupplierDocument supplierDocument);
    
    Result updateSupplier(SupplierDocument supplierDocument);

    Result deleteSupplier(SupplierDocument supplierDocument);

    Result filterSupplier(SupplierFilterRequest request);

    Result assignSupplier(SupplierDocument supplierDocument);

    Result assignSupplierDetails(Map<String, Object> params);

    Result allAssignSupplier(Map<String, Object> params);

    Result importSupplier(MultipartFile file);
}
