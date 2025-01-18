package com.java.email.common;

import java.util.HashMap;

public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    // Getter and Setter methods
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    // Static factory methods
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData((T) new HashMap<String, Object>());
        return result;
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(data != null ? data : (T) new HashMap<String, Object>());
        return result;
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMsg(message);
        result.setData((T) new HashMap<String, Object>());
        return result;
    }
}
