package com.java.email.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReceiverSupplier {
    private String receiverSupplierId; // 收件人ID
    private String receiverSupplierName;
}
