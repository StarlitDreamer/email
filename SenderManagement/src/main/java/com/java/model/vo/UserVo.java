package com.java.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserVo {
    private String userId;
    private String userName;
    private String belongUserName;
    private String userAccount;
    private String userEmail;
    private Integer status;
    private Integer userRole;
}
