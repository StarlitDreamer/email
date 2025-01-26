package com.java.email.Dto;

import com.java.email.entity.Area;
import com.java.email.entity.Country;
import com.java.email.entity.EmailType;
import com.java.email.entity.ReceiverInfo;
import lombok.Data;

import java.util.List;

@Data
public class RequestData {
    private List<EmailType> emailType;
    private List<Area> area;
    private List<Country> country;
    private ReceiverInfo receiverInfo;

    // Getters and Setters
}