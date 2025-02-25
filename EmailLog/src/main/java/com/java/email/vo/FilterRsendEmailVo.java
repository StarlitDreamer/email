package com.java.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class FilterRsendEmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 465468746L;

    private String emailId;
    private String emailTaskId;
    private String subject;
    private String taskType;
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
    private long resend_status;
    private String resend_msg;
    private long resend_start_date;
    private long resend_end_date;



}