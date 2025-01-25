package com.java.email.model.entity.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Document(indexName = "user")
public class UserDocument {
    
    @Id
    private String userId;

    @Field(type = FieldType.Integer)
    private Integer userRole;

    @Field(type = FieldType.Keyword)
    private String creatorId;

    @Field(type = FieldType.Keyword)
    private String belongUserId;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String userName;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String userAccount;

    @Field(type = FieldType.Keyword)
    private String userPassword;

    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String userEmail;

    @Field(type = FieldType.Keyword)
    private String userEmailCode;

    @Field(type = FieldType.Keyword)
    private List<String> userAuthId;

    @Field(type = FieldType.Integer)
    private Integer status;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time_no_millis)
    private String updatedAt;
} 