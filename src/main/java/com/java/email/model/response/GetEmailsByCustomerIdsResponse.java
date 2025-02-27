package com.java.email.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetEmailsByCustomerIdsResponse {
    private String customerId;
    private String customerName;
    private List<String> customerEmails;
}
