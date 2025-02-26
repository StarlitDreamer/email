package com.java.email.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * User entity representing a user.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "user")
public class User {
    @JsonProperty("belong_user_id")
    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private String belongUserid;        // 所属用户ID

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Date)
    private long createdAt;            // 创建日期

    @JsonProperty("creator_id")
    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;          // 创建人ID

    @JsonProperty("status")
    @Field(name = "status", type = FieldType.Integer)
    private Integer status;         // 用户分配状态 1:未分配 2:已分配

    @JsonProperty("updated_at")
    @Field(name = "updated_at", type = FieldType.Date)
    private long updatedAt;            // 更新日期

    @JsonProperty("user_account")
    @Field(name = "user_account", type = FieldType.Keyword)
    private String userAccount;        // 用户登录账号

    @JsonProperty("user_auth_id")
    @Field(name = "user_auth_id", type = FieldType.Keyword)
    private List<String> userAuthid;  // 用户权限ID列表

    @JsonProperty("user_email")
    @Field(name = "user_email", type = FieldType.Keyword)
    private String userEmail;          // 用户邮箱

    @JsonProperty("user_email_code")
    @Field(name = "user_email_code", type = FieldType.Keyword)
    private String userEmailCode;      // 邮箱授权码

    @JsonProperty("user_id")
    @Field(name = "user_id", type = FieldType.Keyword)
    @Id
    private String userId;             // 用户ID

    @JsonProperty("user_name")
    @Field(name = "user_name", type = FieldType.Text)
    private String userName;           // 用户名

    @JsonProperty("user_password")
    @Field(name = "user_password", type = FieldType.Keyword)
    private String userPassword;       // 用户密码，使用MD5加密

    @JsonProperty("user_role")
    @Field(name = "user_role", type = FieldType.Integer)
    private Integer userRole;         // 用户角色 1:公司 2:大管理 3:小管理 4:用户

    @JsonProperty("user_host")
    @Field(name = "user_host", type = FieldType.Keyword)
    private String userHost;           // 用户邮箱的服务器类
}