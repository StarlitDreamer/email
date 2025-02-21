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

    /**
     * 筛选管理员
     * @param params
     * @return Result
     */
    Result filterAdmin(Map<String, Object> params);
}
