package org.easyarch.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTask {
    /**
     * 附件
     */
    private Attachment[] attachment;

    /**
     * 退信数量
     */
    private long bounceAmount;

    /**
     * 创建时间，秒级时间戳
     */
    private long createdAt;

    /**
     * 邮件内容
     */
    private String emailContent;

    /**
     * 主键 值和业务id一样
     */
    private String emailId;

    /**
     * 邮件任务id，使用uuid
     */
    private String emailTaskId;

    /**
     * 邮件类型id，字典里面的邮件类型id
     */
    private String emailTypeId;

    /**
     * 结束时间，秒级时间戳
     */
    private long endDate;

    /**
     * 循环邮件的发送下标，初始值为0
     */
    private long index;

    /**
     * 循环邮件的间隔时间，秒级时间戳
     */
    private long intervalDate;

    /**
     * 收件人id
     */
    private String[] receiverId;

    /**
     * 发件人邮箱
     */
    private String[] senderId;

    /**
     * 僵尸用户邮箱号，此版本不用
     */
    private String[] shadowId;

    /**
     * 开始时间，秒级时间戳
     */
    private long startDate;

    /**
     * 主题
     */
    private String subject;

    /**
     * 任务循环周期，如果是循环发送类型的任务才会填写这个字段
     */
    private Long taskCycle;

    /**
     * 任务类型，0 普通邮件 1 循环邮件 2 定时发送 3
     */
    private Integer taskType;

    /**
     * 模板id
     */
    private String templateId;

    /**
     * 退订数量
     */
    private long unsubscribeAmount;

    // 定义索引映射
    public static TypeMapping createMapping() {
        return new TypeMapping.Builder()
                .properties("attachment", Property.of(p -> p.nested(n -> n)))
                .properties("bounceAmount", Property.of(p -> p.long_(l -> l)))
                .properties("createdAt", Property.of(p -> p.long_(l -> l)))
                .properties("emailContent", Property.of(p -> p.text(t -> t)))
                .properties("emailId", Property.of(p -> p.keyword(k -> k)))
                .properties("emailTaskId", Property.of(p -> p.keyword(k -> k)))
                .properties("emailTypeId", Property.of(p -> p.keyword(k -> k)))
                .properties("endDate", Property.of(p -> p.long_(l -> l)))
                .properties("index", Property.of(p -> p.long_(l -> l)))
                .properties("intervalDate", Property.of(p -> p.long_(l -> l)))
                .properties("receiverId", Property.of(p -> p.keyword(k -> k)))
                .properties("senderId", Property.of(p -> p.keyword(k -> k)))
                .properties("shadowId", Property.of(p -> p.keyword(k -> k)))
                .properties("startDate", Property.of(p -> p.long_(l -> l)))
                .properties("subject", Property.of(p -> p.text(t -> t)))
                .properties("taskCycle", Property.of(p -> p.long_(l -> l)))
                .properties("taskType", Property.of(p -> p.long_(l -> l)))
                .properties("templateId", Property.of(p -> p.keyword(k -> k)))
                .properties("unsubscribeAmount", Property.of(p -> p.long_(l -> l)))
                .build();
    }
}