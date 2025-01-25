package org.easyarch.email.vo;

import co.elastic.clients.elasticsearch.ilm.PutLifecycleRequest;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
/**
 * @author EvoltoStar
 */
@Data
public class EmailVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5022L;

    private String emailId;
    private String emailTaskId;
    private String subject;
    private String taskType;
    private String senderName;
    private String senderEmail;
    private String receiverName;
    private String receiverEmail;
    private String level;
    private String startDate;
    private String endDate;
    private String emailStatus;
    private String errorMsg;
}
