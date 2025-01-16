package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * User entity representing a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private String ownerUserId;        // 所属用户ID
    private long createdAt;            // 创建日期
    private String creatorId;          // 创建人ID
    private UserStatus status;         // 用户分配状态 1:未分配 2:已分配
    private long updatedAt;            // 更新日期
    private String userAccount;        // 用户登录账号
    private List<String> userAuthIds;  // 用户权限ID列表
    private String userEmail;          // 用户邮箱
    private String userEmailCode;      // 邮箱授权码
    private String userId;             // 用户ID
    private String userName;           // 用户名
    private String userPassword;       // 用户密码，使用MD5加密
    private UserRole userRole;         // 用户角色 1:公司 2:大管理 3:小管理 4:用户
}

/**
 * 用户状态枚举
 */
enum UserStatus {
    UNASSIGNED(1),  // 未分配
    ASSIGNED(2);    // 已分配

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

/**
 * 用户角色枚举
 */
enum UserRole {
    COMPANY(1),  // 公司
    ADMIN(2),    // 大管理
    MANAGER(3),  // 小管理
    USER(4);     // 用户

    private final int code;

    UserRole(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}