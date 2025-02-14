package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UndeliveredEmail {
    /**
     * 邮件id，使用uuid
     */
    private String emailId;
    
    /**
     * 邮件任务id，使用uuid
     */
    private String emailTaskId;
    
    /**
     * 发件人id数组，为了支持假用户功能
     * 当前版本数组内只需插入当前用户的id
     */
    private String[] senderId;
    
    /**
     * 收件人id数组
     */
    private String[] receiverId;
    
    /**
     * 错误代码，status为2或3时填写
     */
    private Integer errorCode;
    
    /**
     * 错误信息，status为2或3时填写
     */
    private String errorMsg;

    
    /**
     * 开始时间，秒级时间戳
     */
    private long startDate;
    
    /**
     * 结束时间，秒级时间戳
     */
    private long endDate;

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("emailId", Property.of(p -> p.keyword(k -> k)))
            .properties("emailTaskId", Property.of(p -> p.keyword(k -> k)))
            .properties("senderId", Property.of(p -> p.keyword(k -> k)))
            .properties("receiverId", Property.of(p -> p.keyword(k -> k)))
            .properties("errorCode", Property.of(p -> p.long_(l -> l)))
            .properties("errorMsg", Property.of(p -> p.text(t -> t)))
            .properties("startDate", Property.of(p -> p.long_(l -> l)))
            .properties("endDate", Property.of(p -> p.long_(l -> l)))
            .build();
    }
} 