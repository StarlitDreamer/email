package com.java.email.service.file;

import com.java.email.common.Response.Result;

import java.util.List;
import java.util.Map;

public interface AttachmentService {

    Result uploadAttachment(Map<String, List<Map<String, String>>> request);

    Result assignAttachment(Map<String, Object> request);

    Result assignAttachmentDetails(Map<String, Object> request);

    Result deleteAttachment(Map<String, Object> request);

    Result filterAttachment(Map<String, Object> request);
}
