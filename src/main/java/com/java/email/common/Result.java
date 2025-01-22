package com.java.email.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result<T>{
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data){
        return new Result<>(200,"success", data);
    }

    public static Result success(){
        return new Result(200,"success", null);
    }

    public static Result error(String msg){
        return new Result(400,msg, null);
    }//1
}
