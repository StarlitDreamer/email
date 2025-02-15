package com.java.email.service.receiver;

import com.java.email.common.Response.Result;

import java.util.Map;

public interface ReceiverService {

    Result filterUser(Map<String, Object> request);
   
    Result changeBelongUser(Map<String, Object> request);

    Result getCategory();

    Result filterCommodity(Map<String, Object> request);
}
