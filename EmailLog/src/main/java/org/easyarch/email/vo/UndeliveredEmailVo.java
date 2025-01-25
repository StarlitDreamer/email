package org.easyarch.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author EvoltoStar
 */
@Data
public class UndeliveredEmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5032L;

    private String emailId;
    private String subject;
    private String taskType;
    private String emailType;
    private String senderName;
    private String senderEmail;
    private String receiverName;
    private String receiverEmail;
    private String level;
    private String startDate;
    private String endDate;
    private String errorMsg;

}
