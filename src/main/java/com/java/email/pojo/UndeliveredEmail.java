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

    private String emailId;          // 邮件id，使用uuid
    private String emailTaskId;      // 邮件任务id，使用uuid
    private String[] senderId;       // 发件人id数组
    private String[] receiverId;     // 收件人id
    private int errorCode;          // 错误代码
    private String errorMsg;         // 错误信息
    private String resendCode;       // 重发错误代码
    private String resendEndDate;    // 重发结束时间
    private String resendMsg;        // 重发错误信息
    private String resendStartDate;  // 重发开始时间
    private int resendStatus;       // 邮件重发状态 1:未重发 2:发送中 3:重发成功 4:重发失败
    //private ResendStrategy resendStrategy; // 重发策略

    private long createdAt;          // 创建时间，秒级时间戳
    private long startDate;          // 开始时间，秒级时间戳
    private long endDate;            // 结束时间，秒级时间戳

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("createdAt", Property.of(p -> p.long_(l -> l)))
            .properties("emailId", Property.of(p -> p.keyword(k -> k)))
            .properties("emailTaskId", Property.of(p -> p.keyword(k -> k)))
            .properties("endDate", Property.of(p -> p.long_(l -> l)))
            .properties("errorCode", Property.of(p -> p.long_(l -> l)))
            .properties("errorMsg", Property.of(p -> p.text(t -> t)))
            .properties("receiverId", Property.of(p -> p.keyword(k -> k)))
            .properties("resendCode", Property.of(p -> p.keyword(k -> k)))
            .properties("resendEndDate", Property.of(p -> p.date(d -> d)))
            .properties("resendMsg", Property.of(p -> p.text(t -> t)))
            .properties("resendStartDate", Property.of(p -> p.date(d -> d)))
            .properties("resendStatus", Property.of(p -> p.long_(l -> l)))
            //.properties("resendStrategy", Property.of(p -> p.object(o -> o)))
            .properties("senderId", Property.of(p -> p.keyword(k -> k)))
            .properties("startDate", Property.of(p -> p.long_(l -> l)))
            .build();
    }
} 