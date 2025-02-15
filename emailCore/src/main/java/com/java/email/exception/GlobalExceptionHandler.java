package com.java.email.exception;

import com.java.email.common.Response.Result;
import com.java.email.common.Response.ResultCode;
import com.java.email.utils.LogUtil;
import org.elasticsearch.ElasticsearchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final LogUtil logUtil = LogUtil.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ElasticsearchException.class)
    public Result handleElasticsearchException(ElasticsearchException e) {
        logUtil.error("Elasticsearch operation failed: " + e.getMessage());
        return new Result(ResultCode.R_UpdateDbFailed, "数据库操作失败");
    }

    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        logUtil.error("Runtime error: " + e.getMessage());
        // 其他运行时异常
        return new Result(ResultCode.R_Error, "System error");
    }

    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e) {
        logUtil.error("System error: " + e.getMessage());
        return new Result(ResultCode.R_Error, "System error");
    }
} 