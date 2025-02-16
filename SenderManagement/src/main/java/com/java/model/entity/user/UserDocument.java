package com.java.model.entity.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "user")
public class UserDocument {
    @Id
    @Field(name = "user_id", type = FieldType.Keyword)
    private String userId;

    @Field(name = "user_role", type = FieldType.Integer)
    private Integer userRole;

    @Field(name = "creator_id", type = FieldType.Keyword)
    private String creatorId;

    @Field(name = "belong_user_id", type = FieldType.Keyword)
    private String belongUserId;

    @Field(name = "user_name", type = FieldType.Text, analyzer = "ik_max_word")
    private String userName;

    @Field(name = "user_account", type = FieldType.Text)
    private String userAccount;

    @Field(name = "user_password", type = FieldType.Keyword)
    private String userPassword;

    @Field(name = "user_email", type = FieldType.Text)
    private String userEmail;

    @Field(name = "user_email_code", type = FieldType.Keyword)
    private String userEmailCode;

    @Field(name = "user_host", type = FieldType.Keyword) 
    private String userHost;

    @Field(name = "user_auth_id", type = FieldType.Keyword)
    private List<String> userAuthId;

    @Field(name = "status", type = FieldType.Integer)
    private Integer status;

    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;

    @Field(name = "updated_at", type = FieldType.Long)
    private Long updatedAt;
} 