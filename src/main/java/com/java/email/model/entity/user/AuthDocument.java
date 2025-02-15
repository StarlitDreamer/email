package com.java.email.model.entity.user;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "auth")
public class AuthDocument {
    @Id
    @Field(name = "auth_id", type = FieldType.Keyword)
    private String authId;
    
    @Field(name = "auth_name", type = FieldType.Keyword)
    private String authName;

    // 显式添加 getter 和 setter 方法
    public String getAuthId() {
        return authId;
    }

    public void setAuthId(String authId) {
        this.authId = authId;
    }

    public String getAuthName() {
        return authName;
    }

    public void setAuthName(String authName) {
        this.authName = authName;
    }

    // 可选：添加 toString 方法以便于调试
    @Override
    public String toString() {
        return "AuthDocument{" +
                "authId='" + authId + '\'' +
                ", authName='" + authName + '\'' +
                '}';
    }
} 