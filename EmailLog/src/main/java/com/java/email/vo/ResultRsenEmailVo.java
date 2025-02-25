package com.java.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ResultRsenEmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 144874654L;

    private long total_items;
    private int page_num;
    private int page_size;
    List<FilterRsendEmailVo> email_info;
}
