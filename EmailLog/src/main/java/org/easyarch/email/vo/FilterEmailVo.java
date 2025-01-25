package org.easyarch.email.vo;

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
    private String taskType;
    private String senderName;
    private String senderEmail;
    private String receiverName;
    private String receiverEmail;
    private String receiverBirth;
    private int receiverLevel;
    private String startDate;
    private String endDate;
    private int emailStatus;
    private int page;
    private int size;
}
