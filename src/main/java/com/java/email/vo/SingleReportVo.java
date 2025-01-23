package com.java.email.vo;

import lombok.Data;

import java.io.Serializable;
@Data
public class SingleReportVo implements Serializable {

    private int count;
    private int success;
    private int fail;
    private String sendrate;
}
