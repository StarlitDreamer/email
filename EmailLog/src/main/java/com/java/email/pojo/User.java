package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    /**
     * 所属用户，该用户所属于哪个管理员的uuid
     */
    @JsonProperty("belong_user_id")
    private String belongUserid;

    /**
     * 创建日期
     */
    @JsonProperty("created_at")
    private String createdAt;

    /**
     * 创建人ID
     */
    @JsonProperty("creator_id")
    private String creatorid;

    /**
     * 用户分配状态 1:未分配 2:已分配
     */
    private Integer status;

    /**
     * 更新日期
     */
    @JsonProperty("updated_at")
    private String updatedAt;

    /**
     * 用户登录账号
     */
    @JsonProperty("user_account")
    private String userAccount;

    /**
     * 用户权限ID数组
     */
    @JsonProperty("user_authid")
    private String[] userAuthid;

    /**
     * 用户邮箱
     */
    @JsonProperty("user_email")
    private String userEmail;

    /**
     * 邮箱授权码
     */
    @JsonProperty("user_email_code")
    private String userEmailCode;

    /**
     * 用户ID，使用uuid
     */
    @JsonProperty("user_id")
    private String userid;

    /**
     * 用户名
     */
    @JsonProperty("user_name")
    private String userName;

    /**
     * 用户密码，使用md5加密
     */
    @JsonProperty("user_password")
    private String userPassword;

    /**
     * 用户角色 1:公司 2:大管理 3:小管理 4:用户
     */
    @JsonProperty("user_role")
    private Integer userRole;

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("belong_user_id", Property.of(p -> p.keyword(k -> k)))
            .properties("created_at", Property.of(p -> p.keyword(k -> k)))
            .properties("creator_id", Property.of(p -> p.keyword(k -> k)))
            .properties("status", Property.of(p -> p.integer(i -> i)))
            .properties("updated_at", Property.of(p -> p.keyword(k -> k)))
            .properties("user_account", Property.of(p -> p.keyword(k -> k)))
            .properties("user_authid", Property.of(p -> p.keyword(k -> k)))
            .properties("user_email", Property.of(p -> p.keyword(k -> k)))
            .properties("user_email_code", Property.of(p -> p.keyword(k -> k)))
            .properties("user_id", Property.of(p -> p.keyword(k -> k)))
            .properties("user_name", Property.of(p -> p.text(t -> t
                .fields("keyword", f -> f.keyword(k -> k)))))
            .properties("user_password", Property.of(p -> p.keyword(k -> k)))
            .properties("user_role", Property.of(p -> p.integer(i -> i)))
            .build();
    }
} 