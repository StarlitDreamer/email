package com.java.email.common;

import lombok.Data;
import java.util.HashMap;

@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static Result<HashMap<String, Object>> success() {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setCode(200);
        result.setMsg("成功");
        result.setData(new HashMap<>());
        return result;
    }

    public static Result<HashMap<String, Object>> error(String msg) {
        Result<HashMap<String, Object>> result = new Result<>();
        result.setCode(500);
        result.setMsg(msg);
        result.setData(new HashMap<>());
        return result;
    }



    // Getter 和 Setter 方法
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
}
