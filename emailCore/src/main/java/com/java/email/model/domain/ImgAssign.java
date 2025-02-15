package com.java.email.model.domain;

import lombok.Data;

import java.util.List;

@Data
public class ImgAssign {
    private String imgId;
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