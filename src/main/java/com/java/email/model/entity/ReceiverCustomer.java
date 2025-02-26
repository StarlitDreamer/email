package com.java.email.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverCustomer {
    private String receiverCustomerId; // 收件人ID
    private String receiverCustomerName;
}
