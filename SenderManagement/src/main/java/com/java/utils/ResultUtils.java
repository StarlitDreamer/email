package com.java.utils;

import com.java.model.domain.Result;
import org.springframework.http.HttpStatus;

public  final class ResultUtils {

    // 快速返回操作成功响应结果(带响应数据)
    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(HttpStatus.OK.value())
                .setMessage("操作成功")
                .setData(data);
    }

    // 快速返回操作成功响应结果
    public static <T> Result<T>success() {
        return new Result<T>()
                .setCode(HttpStatus.OK.value())
                .setMessage("操作成功")
                .setData(null);
    }

    // 返回操作失败响应结果
    public static<T> Result<T> error(String message) {
        return new Result<T>()
                .setCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setMessage(message)
                .setData(null);
    }

    // 返回带数据的错误响应
    public static <T> Result<T> error(String message, T data) {
        return new Result<T>()
                .setCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setMessage(message)
                .setData(data);
    }

}
