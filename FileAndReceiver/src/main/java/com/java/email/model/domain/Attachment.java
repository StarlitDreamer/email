package com.java.email.model.domain;

import lombok.Data;
import java.util.List;

@Data
public class Attachment {
    private String attachmentId;
    private String attachmentUrl;
    private String attachmentSize;
    private String attachmentName;
    private String creatorId;
    private List<String> belongUserId;
    private Integer status;
    private String createdAt;
    private String updatedAt;
} 