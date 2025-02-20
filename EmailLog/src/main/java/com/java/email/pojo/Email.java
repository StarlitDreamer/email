package com.java.email.pojo;

import co.elastic.clients.elasticsearch._types.mapping.Property;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * 创建时间，秒级时间戳
     */
    @JsonProperty("created_at")
    private long createdAt;
    /**
     * 邮件id，使用uuid
     */
    @JsonProperty("email_id")
    private String emailId;
    /**
     * 邮件状态，使用数字1开始 2暂停 3终止 4重置 5异常 6完成
     */
    @JsonProperty("email_status")
    private long emailStatus;
    /**
     * 邮件任务id，使用uuid
     */
    @JsonProperty("email_task_id")
    private String emailTaskId;
    /**
     * 状态修改时间，秒级时间戳
     */
    @JsonProperty("update_at")
    private Long updateAt;

//    // 定义索引映射
//    public static TypeMapping createMapping() {
//        return new TypeMapping.Builder()
//            .properties("createdAt", Property.of(p -> p.long_(l -> l)))
//            .properties("emailId", Property.of(p -> p.keyword(k -> k)))
//            .properties("emailStatus", Property.of(p -> p.long_(l -> l)))
//            .properties("emailTaskId", Property.of(p -> p.keyword(k -> k)))
//            .properties("endDate", Property.of(p -> p.long_(l -> l)))
//            .properties("errorCode", Property.of(p -> p.long_(l -> l)))
//            .properties("errorMsg", Property.of(p -> p.text(t -> t)))
//            .properties("receiverId", Property.of(p -> p.keyword(k -> k)))
//            .properties("senderId", Property.of(p -> p.keyword(k -> k)))
//            .properties("startDate", Property.of(p -> p.long_(l -> l)))
//                .properties("receiverName", Property.of(p -> p.keyword(t -> t)))
//                .properties("senderName", Property.of(p -> p.keyword(t -> t)))
//            .build();
//    }
} 