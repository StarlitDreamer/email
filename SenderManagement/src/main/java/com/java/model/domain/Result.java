package com.java.model.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;


@Data
@Accessors(chain = true)
public class Result<T> {
    private Integer code; // 业务状态码 0-成功, 非0-失败
    private String message; // 提示信息
    private T data; // 响应数据
}
