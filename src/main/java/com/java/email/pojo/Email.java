package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Email implements Serializable {
    private static final long serialVersionUID = 1000L;

    private String emailId;          // 邮件id，使用uuid
    private int emailStatus;        // 邮件状态 1:已送达 2:已打开 3:未送达 4:已退信 5:已退订
    private String emailTaskId;      // 邮件任务id，使用uuid
    private int errorCode;          // 错误代码，status为2或3时再填写
    private String errorMsg;         // 错误信息，status为2或3时再填写
    private String[] receiverId;     // 收件人id
    private String[] senderId;       // 发件人id数组
    private long createdAt;          // 创建时间，秒级时间戳
    private long startDate;          // 开始时间，秒级时间戳
    private long endDate;            // 结束时间，秒级时间戳

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("createdAt", Property.of(p -> p.long_(l -> l)))
            .properties("emailId", Property.of(p -> p.keyword(k -> k)))
            .properties("emailStatus", Property.of(p -> p.long_(l -> l)))
            .properties("emailTaskId", Property.of(p -> p.keyword(k -> k)))
            .properties("endDate", Property.of(p -> p.long_(l -> l)))
            .properties("errorCode", Property.of(p -> p.long_(l -> l)))
            .properties("errorMsg", Property.of(p -> p.text(t -> t)))
            .properties("receiverId", Property.of(p -> p.keyword(k -> k)))
            .properties("senderId", Property.of(p -> p.keyword(k -> k)))
            .properties("startDate", Property.of(p -> p.long_(l -> l)))
            .build();
    }
} 