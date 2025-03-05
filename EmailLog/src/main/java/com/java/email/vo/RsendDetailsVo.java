package com.java.email.vo;

import lombok.Data;

import java.util.Set;
@Data
public class RsendDetailsVo {
    private Set<String> recipientEmails;
    private Set<String> resendTaskIds;
    private Set<String> resendEmailIds;
}
