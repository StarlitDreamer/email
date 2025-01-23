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

    private String emailTaskId;
    private int taskType;
    private String subject;
    private String senderName;
    private String senderEmail;
    private int taskStatus;
    private int operateStatus;
    private String startDate;
    private String endDate;
    private int page;
    private int size;

}
