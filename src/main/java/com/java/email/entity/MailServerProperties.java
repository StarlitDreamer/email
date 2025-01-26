package com.java.email.entity;

import lombok.Data;

@Data
public class MailServerProperties {

    private boolean sslEnabled; // 是否启用 SSL
    private int connectionTimeout; // 连接超时时间（毫秒）
    private int timeout; // 超时时间（毫秒）
    private String authMethod; // 认证方式（如 PLAIN, LOGIN, CRAM-MD5）
    private boolean starttlsEnabled; // 是否启用 STARTTLS
    private boolean debugEnabled; // 是否启用调试模式

    // 可以根据需要添加更多字段
}