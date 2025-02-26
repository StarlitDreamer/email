package com.java.email.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "email_report")
public class EmailReport {

    @JsonProperty("bounce_amount")
    @Field(name = "bounce_amount", type = FieldType.Long)
    private Long bounceAmount;  // 退信数量

    @JsonProperty("delivery_amount")
    @Field(name = "delivery_amount", type = FieldType.Long)
    private Long deliveryAmount;  // 送达数量

    @Id
    @JsonProperty("email_task_id")
    @Field(name = "email_task_id", type = FieldType.Keyword)
    private String emailTaskId;  // 邮件任务ID

    @JsonProperty("email_total")
    @Field(name = "email_total", type = FieldType.Long)
    private Long emailTotal;  // 具体邮件总数

    @JsonProperty("open_amount")
    @Field(name = "open_amount", type = FieldType.Long)
    private Long openAmount;  // 打开数量

    @JsonProperty("unsubscribe_amount")
    @Field(name = "unsubscribe_amount", type = FieldType.Long)
    private Long unsubscribeAmount;  // 退订数量
}
