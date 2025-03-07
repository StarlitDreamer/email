package com.java.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
@Data
public class FilterEmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5065L;

    private String emailId;
    private String emailTaskId;
    private String subject;
    private long task_type;
    private String sender_name;
    private String sender_email;
    private String receiver_name;
    private String receiver_email;
    private String receiver_birth;
    private long receiver_level;
    private String start_date;
    private String end_date;
    private int email_status;
    private String error_msg;

}
