package com.java.email.service.receiver;

import java.util.Map;

import com.java.email.common.Response.Result;

public interface ReceiverService {

    Result filterUser(Map<String, Object> request);
   
    Result changeBelongUser(Map<String, Object> request);

    Result getCategory();

    Result filterCommodity(Map<String, Object> request);
}
