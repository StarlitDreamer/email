package com.java.email.entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "mail_server_config")
public class MailServerConfig {

    @Id
    private String id;//邮件服务器配置id

    @Field(type = FieldType.Keyword)
    private String name;//邮件服务器配置name

    @Field(type = FieldType.Keyword)
    private String host;//邮件服务器配置主机

    @Field(type = FieldType.Integer)
    private int port;//邮件服务器配置port

    @Field(type = FieldType.Keyword)
    private String username;//邮件服务器配置username

    @Field(type = FieldType.Keyword)
    private String encryptedPassword;// 存储加密后的密码

    @Field(type = FieldType.Keyword)
    private int protocol;   // 0：SMTP,     1： IMAP,      2： POP3

    @Field(type = FieldType.Object)
    private MailServerProperties properties;

    @Field(type = FieldType.Integer)
    private int dailyLimit; // 每日限额

    @Field(type = FieldType.Integer)
    private int emailInterval; // 邮件间隔（秒）

    @Field(type = FieldType.Integer)
    private int batchSize; // 批量发送数量

    @Field(type = FieldType.Integer)
    private int batchInterval; // 批次间隔（分钟）

}