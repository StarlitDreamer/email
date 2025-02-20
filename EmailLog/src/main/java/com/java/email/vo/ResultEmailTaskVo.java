package com.java.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
@Data
public class ResultEmailTaskVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5595657947619230L;

    private long total_items;
    private int page_num;
    private int page_size;
    List<FilterTaskVo> task_info;
}
