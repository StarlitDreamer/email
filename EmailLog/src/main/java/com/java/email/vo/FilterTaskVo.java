package com.java.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * @author EvoltoStar
 */
@Data
public class FilterTaskVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5052L;

    private String task_id;
    private int taskType;
    private String subject;
    private String sender_name;
    private String sender_email;
    private String email_type_name;
    private long task_status;
    private String start_date;
    private String end_date;


}
