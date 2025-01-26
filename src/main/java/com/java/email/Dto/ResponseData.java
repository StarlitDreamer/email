package com.java.email.Dto;

import com.java.email.entity.Area;
import com.java.email.entity.Country;
import com.java.email.entity.EmailType;
import com.java.email.entity.ReceiverInfo;
import lombok.Data;

import java.util.List;

@Data
public class ResponseData {
    private List<EmailType> emailType;
    private List<Area> area;
    private List<Country> country;
    private ReceiverInfo receiverInfo;

    public ResponseData(List<EmailType> emailType, List<Area> area, List<Country> country, ReceiverInfo receiverInfo) {
        this.emailType = emailType;
        this.area = area;
        this.country = country;
        this.receiverInfo = receiverInfo;
    }
}
