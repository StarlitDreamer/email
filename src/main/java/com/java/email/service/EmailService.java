package com.java.email.service;

import com.java.email.common.Result;
import com.java.email.model.EmailTypeCreateRequest;
import com.java.email.model.EmailTypeFilterRequest;

public interface EmailService {
    Result<?> createEmail(String emailTypeName);
    Result<?> filterEmailType(EmailTypeFilterRequest request);
}