package com.java.email.service.receiver;

import com.java.email.common.Response.Result;
import com.java.email.model.entity.receiver.CustomerDocument;
import com.java.email.model.dto.request.CustomerFilterRequest;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

public interface CustomerService {
    Result createCustomer(CustomerDocument customerDocument);
    Result updateCustomer(CustomerDocument customerDocument);
    Result deleteCustomer(CustomerDocument customerDocument);
    Result filterCustomer(CustomerFilterRequest request);
    Result assignCustomer(CustomerDocument customerDocument);
    Result assignCustomerDetails(Map<String, Object> params);
    Result allAssignCustomer(Map<String, Object> params);
    Result importCustomer(MultipartFile file);
}
