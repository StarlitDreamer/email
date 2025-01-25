package com.java.email.service.email;

import com.java.email.common.Response.Result;
import java.util.Map;

public interface TemplateService {
    Result saveTemplate(Map<String, Object> request);
    Result assignTemplate(Map<String, Object> request);
    Result assignTemplateDetails(Map<String, Object> request);
    Result filterTemplate(Map<String, Object> request);
    Result deleteTemplate(Map<String, Object> request);
} 