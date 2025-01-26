package com.java.email.common;

import com.java.email.Dto.ResponseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Response {
    private int code;
    private String msg;
    private ResponseData data;
}