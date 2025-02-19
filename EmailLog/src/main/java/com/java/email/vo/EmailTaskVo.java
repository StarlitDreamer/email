package com.java.email.vo;

import com.java.email.pojo.EmailTask;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * @author EvoltoStar
 */
@Data
public class EmailTaskVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5012L;
    private List<EmailTask> emailTask;
    private long total;
}
