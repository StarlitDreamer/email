package com.java.email.service;

import com.java.email.common.Result;

public interface EmailService {
    Result<?> createEmail(String emailTypeName);
}