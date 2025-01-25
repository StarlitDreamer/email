package org.easyarch.email.result;

import lombok.Data;

@Data
public class Result<T> {
    //状态码
    private Integer code;
    //信息
    private String message;
    //数据
    private T data;

    private Result() {

    }
    //写的不好
//    public static <T> Result<T> build(ResultCodeEnum resultCodeEnum) {
//        Result<T> result=new Result<>();
//        result.setCode(200);
//        result.setData(null);
//        result.setMessage(resultCodeEnum.getMessage());
//        return  result;
//    }

    //设置数据的方法
    public static <T> Result<T> build(T data, ResultCodeEnum resultCodeEnum) {
        Result<T> result = new Result<>();
        if (data != null) {
            result.setData(data);

        }
        result.setCode(resultCodeEnum.getCode());
        result.setMessage(resultCodeEnum.getMessage());
        return result;
    }

    public static <T> Result<T> build(T data,Integer code,String message) {
        Result<T> result = new Result<>();
        if (data != null) {
            result.setData(data);

        }
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static<T> Result<T> ok(){

        return build(null, ResultCodeEnum.SUCCESS);
    }
    public static<T> Result<T> ok(T data){
        Result<T> result = build(data, ResultCodeEnum.SUCCESS);

        return result;
    }
    public static <T> Result<T> fail(T data){
        Result<T> result=build(data,ResultCodeEnum.FAIL);
        return  result;
    }
}
