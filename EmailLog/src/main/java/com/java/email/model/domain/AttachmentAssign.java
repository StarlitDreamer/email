package com.java.email.model.domain;

import lombok.Data;

import java.util.List;

@Data
public class AttachmentAssign {
    private String attachmentId;
    private List<AssignProcess> assignProcess;

    @Data
    public static class AssignProcess {
        private String assignorId;
        private String assignorName;
        private List<Assignee> assignee;
        private String assignDate;
    }

    @Data
    public static class Assignee {
        private String assigneeId;
        private String assigneeName;
    }
} 