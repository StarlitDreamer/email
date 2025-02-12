package com.java.email.model.domain;

import lombok.Data;
import java.util.List;

@Data
public class User {
    private String userId;
    private Integer userRole;
    private String creatorId;
    private String belongUserId;
    private String userName;
    private String userAccount;
    private String userPassword;
    private String userEmail;
    private String userEmailCode;
    private List<String> userAuthId;
    private Integer status;
    private String createdAt;
    private String updatedAt;
} 