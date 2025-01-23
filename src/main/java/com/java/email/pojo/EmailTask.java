package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTask {
    private Attachment[] attachment;      // 附件
    private String emailContent;         // 邮件内容
    private String emailTaskId;          // 邮件任务id，使用uuid
    private String emailTypeId;          // 邮件类型id，字典里面的邮件类型id
    private int operateStatus;          // 任务操作状态 1:开始态 2:暂停态 3:终止态 4:重置态
    private String[] receiverId;         // 收件人id
    private String[] senderId;           // 发件人id数组
    private long createdAt;              // 创建时间，秒级时间戳
    private long startDate;              // 开始时间，秒级时间戳
    private long endDate;                // 结束时间，秒级时间戳
    private String subject;              // 主题
    private int taskCycle;              // 任务循环周期
    private int taskStatus;             // 任务状态 1:发送中 2:发送暂停 3:发送终止 4:发送重置 5:发送成功 6:发送失败
    private int taskType;               // 任务类型 1:手动发送 2:循环发送 3:节日发送 4:生日发送
    private String templateId;           // 模板id
    private long bounceAmount;           // 退信数量
    private long unsubscribeAmount;      // 退订数量

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
            .properties("attachment", Property.of(p -> p.nested(o -> o)))
            .properties("bounceAmount", Property.of(p -> p.long_(l -> l)))
            .properties("createdAt", Property.of(p -> p.long_(l -> l)))
            .properties("emailContent", Property.of(p -> p.text(t -> t)))
            .properties("emailTaskId", Property.of(p -> p.keyword(k -> k)))
            .properties("emailTypeId", Property.of(p -> p.keyword(k -> k)))
            .properties("endDate", Property.of(p -> p.long_(l -> l)))
            .properties("operateStatus", Property.of(p -> p.long_(l -> l)))
            .properties("receiverId", Property.of(p -> p.keyword(k -> k)))
            .properties("senderId", Property.of(p -> p.keyword(k -> k)))
            .properties("startDate", Property.of(p -> p.long_(l -> l)))
            .properties("subject", Property.of(p -> p.text(t -> t)))
            .properties("taskCycle", Property.of(p -> p.long_(l -> l)))
            .properties("taskStatus", Property.of(p -> p.long_(l -> l)))
            .properties("taskType", Property.of(p -> p.long_(l -> l)))
            .properties("templateId", Property.of(p -> p.keyword(k -> k)))
            .properties("unsubscribeAmount", Property.of(p -> p.long_(l -> l)))
            .build();
    }
} 