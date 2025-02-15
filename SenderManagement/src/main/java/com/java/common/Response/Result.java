package com.java.common.Response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class Result {
    private int code;//状态码
    private String msg;//提示信息
    private Object data;//响应数据

    public Result(ResultCode resultCode){
        this.code=resultCode.getCode();
        this.msg= resultCode.getMsg();
    }

    public Result(ResultCode resultCode, Object data){
        this.code=resultCode.getCode();
        this.msg= resultCode.getMsg();
        this.data=data;
    }

    public void setResultCode(ResultCode resultCode){
        this.code=resultCode.getCode();
    }
}
