package com.java.email.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 邮件管理实体类。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email")
public class Email {
    @Id
    @Field(name = "email_id", type = FieldType.Keyword)
    private String emailId;              // 邮件ID，使用UUID，不分词

    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;          // 邮件任务ID，使用UUID，不分词

    @JsonProperty("created_at")
    @Field(name = "created_at", type = FieldType.Long)
    private Long createdAt;              // 创建时间，秒级时间戳

    @JsonProperty("update_at")
    @Field(name = "update_at", type = FieldType.Long)
    private Long updateAt;               // 状态修改时间，秒级时间戳

    @JsonProperty("email_status")
    @Field(name = "email_status", type = FieldType.Integer)
    private Integer emailStatus;         // 邮件状态：1-开始，2-暂停，3-终止，4-重置，5-异常，6-完成
}
