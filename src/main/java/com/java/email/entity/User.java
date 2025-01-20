package com.java.email.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.util.List;

/**
 * User entity representing a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "users")
public class User {
    private String belongUserid;        // 所属用户ID  ownerUserId
    private long createdAt;            // 创建日期
    private String creatorId;          // 创建人ID
    private int status;         // 用户分配状态 1:未分配 2:已分配
    private long updatedAt;            // 更新日期
    private String userAccount;        // 用户登录账号
    private List<String> userAuthid;  // 用户权限ID列表  userAuthIds
    private String userEmail;          // 用户邮箱
    private String userEmailCode;      // 邮箱授权码
    @Id
    private String userId;             // 用户ID
    private String userName;           // 用户名
    private String userPassword;       // 用户密码，使用MD5加密
    private int userRole;         // 用户角色 1:公司 2:大管理 3:小管理 4:用户
}