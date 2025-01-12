package com.java.email.service;

import com.java.email.common.Response.Result;

import java.util.List;
import java.util.Map;

public interface AttachmentService {

    Result uploadAttachment(Map<String, List<Map<String, String>>> request);

    Result assignAttachment(Map<String, Object> request);
}