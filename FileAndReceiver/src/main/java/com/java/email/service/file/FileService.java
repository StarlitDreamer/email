package com.java.email.service.file;

import com.java.email.common.Response.Result;
import java.util.Map;

public interface FileService {
    
    /**
     * 筛选用户
     * @param params
     * @return Result
     */
    Result filterUser(Map<String, Object> params);
}
