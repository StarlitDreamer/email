package org.easyarch.email.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * @author EvoltoStar
 */
@Data
public class EmailTaskVo implements Serializable {
    @Serial
    private static final long serialVersionUID = 5012L;

    private String emailTaskId;
    private String subject;
    private int taskType;
    private String emailTypeId;
    private String senderName;
    private int taskStatus;
    private String createdAt;
}
