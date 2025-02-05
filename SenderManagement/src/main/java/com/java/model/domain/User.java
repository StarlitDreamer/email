package com.java.model.domain;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "user")
public class User {
    @Id
    @Field(type = FieldType.Keyword,name="user_id")
    private String userId; // 用户id

    @Field(type = FieldType.Integer,name="user_role")
    private int userRole; // 用户角色

    @Field(type = FieldType.Keyword,name="creator_id")
    private String creatorId; // 创建者id

    @Field(type = FieldType.Keyword,name="belong_user_id")
    private String belongUserId; // 所属用户id

    @Field(type = FieldType.Text,name="user_name")
    private String userName; // 用户名

    @Field(type = FieldType.Keyword,name="user_account")
    private String userAccount; // 用户登录账号

    @Field(type = FieldType.Keyword,name = "user_password")
    private String userPassword; // 用户登录密码

    @Field(type = FieldType.Keyword,name = "user_email")
    private String userEmail; // 用户邮箱

    @Field(type = FieldType.Keyword,name = "user_email_code")
    private String userEmailCode; // 用户邮箱授权码

    @Field(type = FieldType.Keyword,name = "user_auth_id")
    private List<String> userAuthId; // 用户权限id

    @Field(type = FieldType.Integer,name = "status")
    private int status; // 用户分配的状态

    @Field(type = FieldType.Keyword,name = "created_at")
    private String createdAt; // 创建时间

    @Field(type = FieldType.Keyword,name = "updated_at")
    private String updatedAt; // 更新时间
    @Field(type = FieldType.Keyword,name = "user_host")
    private String userHost;//邮箱类型
}
