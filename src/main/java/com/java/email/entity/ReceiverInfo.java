package com.java.email.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReceiverInfo {
    private String receiverName;
    private String contactPerson;
    private String contactWay;
    private String receiverCountry;
    private String tradeType;
    private String sex;
    private String birth;
}
